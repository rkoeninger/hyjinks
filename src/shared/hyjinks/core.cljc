(ns hyjinks.core
  (:require [clojure.string :refer [escape split join capitalize lower-case trim index-of]]
            [hyjinks.macros #?(:clj :refer :cljs :refer-macros) [deftag deftags extend-type-ifn defrecord-ifn]]))

;; Forward definitions to resolve circular references

(declare tag?)
(declare extend-tag)

;; General helpers

(defn- prune-map [m]
  (let [rem? #(or (nil? %) (= "" %) (and (coll? %) (empty? %)))]
    (apply dissoc m (map first (filter (comp rem? second) m)))))

(defn- str-join [& items] (join (flatten items)))

(defn- interposep [sep pred [x & [y :as more] :as coll]]
  (cond
    (empty? coll)      (empty coll)
    (= 1 (count coll)) coll
    (pred x y)         (concat [x sep] (interposep sep pred more))
    :else              (concat [x] (interposep sep pred more))))

(defn- str-join-extra-spaces [& items]
  (->> items
    flatten
    (interposep " " #(not (or (tag? %1) (tag? %2))))
    (apply str)))

(defn- starts-with [prefix s] (= prefix (subs s 0 (#?(:clj .length :cljs .-length) prefix))))

(def ^{:private true} escape-chars {
  \< "&lt;"
  \> "&gt;"
  \& "&amp;"
  \" "&quot;"
  \' "&#39;"})

(defn- html-escape [x]
  (if (string? x)
    (escape x escape-chars)
    x))

(defn- split-selector [s]
  (if (not= s "")
    (let [i (index-of s "." 1)
          j (index-of s "#" 1)
          k (cond
              (not (or i j)) nil
              (not i) j
              (not j) i
              (< j i) j
              (< i j) i)]
      (if k
        (cons (subs s 0 k) (split-selector (subs s k)))
        (cons s nil)))))

(defn- parse-selector [selector]
  (let [selector-parts (split-selector (name selector))
        tag-name       (first (filter #(not (or (starts-with "#" %) (starts-with "." %))) selector-parts))
        id-clause      (first (filter (partial starts-with "#") selector-parts))
        class-clauses  (filter (partial starts-with ".") selector-parts)]
    {:tag-name    (or tag-name "div")
     :id          (if id-clause (subs id-clause 1))
     :class-names (mapv #(subs % 1) class-clauses)}))

(defn tag->string
  "Serializes a tag and its children into an HTML string."
  [{:keys [tag-name attrs css r-opts items]}]
  (let [{css-class :class} attrs
        attrs (if (sequential? css-class) (assoc attrs :class (join " " css-class)) attrs)
        attrs+css (if (empty? css) attrs (assoc attrs :style (str css)))
        escape-child (if (:no-escape r-opts) identity html-escape)
        child-join (if (:pad-children r-opts) str-join-extra-spaces str-join)]
    (str-join
      (if (= tag-name "html") "<!DOCTYPE html>")
      "<" tag-name attrs+css
      (if (:void-element r-opts)
        ">"
        [">" (child-join (map escape-child items)) "</" tag-name ">"]))))

(defn tag->hiccup
  "Translates a tag to the nested-vector representation used by hiccup."
  [{:keys [tag-name attrs css r-opts items]}]
  (let [{css-class :class} attrs
        attrs (if (sequential? css-class) (assoc attrs :class (join " " css-class)) attrs)
        attrs+css (if (empty? css) attrs (assoc attrs :style (str css)))
        escape-child (if (:no-escape r-opts) identity html-escape)
        child-join (if (:pad-children r-opts) str-join-extra-spaces str-join)]
    (vec
      (concat
        (if (empty? attrs+css)
          [(keyword tag-name)]
          [(keyword tag-name) (into {} attrs+css)])
        (map #(if (tag? %) (tag->hiccup %) %) items)))))

;; Core types

(defrecord Literal [s]
  #?(:clj java.lang.Object :cljs Object)
  (toString [_] s))

(defrecord Comment [content]
  #?(:clj java.lang.Object :cljs Object)
  (toString [_] (str "<!-- " content " -->")))

(defrecord RenderOptions []
  #?(:clj java.lang.Object :cljs Object)
  (toString [this]
    (str-join (filter (fn [[k v]] (if v [k v])) this))))

(defrecord Attrs []
  #?(:clj java.lang.Object :cljs Object)
  (toString [this]
    (str-join (map (fn [[k v]] [" " (name k) "=\"" (html-escape v) "\""]) this)))
  #?@(:clj [
  clojure.lang.IFn
  (invoke [this] this)
  (invoke [this t] (extend-tag t this))
  (applyTo [this args] (extend-tag (first args) this))]))

(defrecord Css []
  #?(:clj java.lang.Object :cljs Object)
  (toString [this]
    (str-join (map (fn [[k v]] ["; " (name k) ": " v]) this) ";"))
  #?@(:clj [
  clojure.lang.IFn
  (invoke [this] this)
  (invoke [this t] (extend-tag t this))
  (applyTo [this args] ((first args) this))]))

(#?(:clj defrecord-ifn :cljs defrecord) Tag [tag-name attrs css items r-opts]
  #?(:clj java.lang.Object :cljs Object)
  (toString [this] (tag->string this)))

(do #?@(
  :cljs
  [(extend-type Attrs
    cljs.core/IFn
    (-invoke
      ([this] this)
      ([this t] (extend-tag t this))))
  (extend-type Css
    cljs.core/IFn
    (-invoke
      ([this] this)
      ([this t] (extend-tag t this))))
  (extend-type-ifn Tag)]))

;; Builder functions

(defn literal?
  "Checks if argument is a Literal and will be passed
   through rendering without being escaped or encoded."
  [x]
  (instance? Literal x))

(defn comment?
  "Checks if argument is an HTML Comment."
  [x]
  (instance? Comment x))

(defn tag?
  "Checks if argument is a Tag."
  [x]
  (instance? Tag x))

(defn css?
  "Checks if argument is a collection of CSS attributes."
  [x]
  (instance? Css x))

(defn attrs?
  "Checks if argument is a collection of HTML attributes."
  [x]
  (instance? Attrs x))

(defn r-opts?
  "Checks if argument is a collection of Rendering Options."
  [x]
  (instance? RenderOptions x))

(defn- attrs-or-map? [x] (or (attrs? x) (and (map? x) (not (record? x)))))

(defn- child-item? [x] (not (or (attrs-or-map? x) (css? x) (r-opts? x) (nil? x) (= "" x))))

(defn- join-class-names [x]
  (if (sequential? x)
    (->> x
      (filter identity)
      (map trim)
      (join " "))
    x))

(defn- merge-attrs [xs ys]
  (let [m (merge xs ys)
        cs0 (join-class-names (:class xs))
        cs1 (join-class-names (:class ys))]
    (assoc m :class
      (if (and cs0 cs1)
        (str cs0 " " cs1)
        (join-class-names (:class m))))))

(defn- extend-attrs [attrs more] (prune-map (reduce merge-attrs attrs more)))

(defn- extend-css [css more] (prune-map (apply merge css more)))

(defn- extend-r-opts [r-opts more] (prune-map (apply merge r-opts more)))

(defn- extend-items [items more] (vec (flatten (concat items more))))

(defn attrs
  "Creates a new collection of HTML Attributes from the given
   sequence of key-values."
  [& {:as key-vals}]
  (extend-attrs (Attrs.) key-vals))

(defn css
  "Creates a new collection of CSS Attributes from the given
   sequence of key-values."
  [& {:as key-vals}]
  (extend-css (Css.) key-vals))

(defn- r-opts [& {:as key-vals}] (extend-r-opts (RenderOptions.) key-vals))

(defn tag
  "Creates a new Tag with the given child tags, HTML Attributes,
   CSS Attributes and Rendering Options. Tag name argument can
   use CSS selector syntax to provide an id and CSS class names
   in addition to a tag name."
  [selector & stuff]
  (let [stuff (flatten stuff)
        {:keys [tag-name id class-names]} (parse-selector selector)
        attrs  (extend-attrs
                 (Attrs.)
                 (conj
                  (filter attrs-or-map? stuff)
                  (if id {:id id})
                  (if (seq class-names) {:class class-names})))
        css    (extend-css    (Css.)           (filter css? stuff))
        r-opts (extend-r-opts (RenderOptions.) (filter r-opts? stuff))
        items  (extend-items  []               (filter child-item? stuff))]
    (Tag. tag-name attrs css items r-opts)))

(defn extend-tag
  "Merges a tag with additional child tags, HTML Attributes,
   CSS Attributes and Rendering Options."
  [{:keys [tag-name attrs css r-opts items] :as t} & stuff]
  (let [stuff (flatten stuff)]
    (if (empty? stuff)
      t
      (let [attrs  (extend-attrs  attrs  (filter attrs-or-map? stuff))
            css    (extend-css    css    (filter css? stuff))
            r-opts (extend-r-opts r-opts (filter r-opts? stuff))
            items  (extend-items  items  (filter child-item? stuff))]
        (Tag. tag-name attrs css items r-opts)))))

(defn literal
  "Creates a new Literal that will be passed through rendering without
   being escaped or encoded. It adds no additional space between arguments."
  [& content]
  (Literal. (str-join content)))

;; Declaring rendering options

(def void-element
  "Indicates that this tag cannot have children and will
   be rendered ending with /> instead of a closing tag."
  (r-opts :void-element true))

(def no-escape
  "Prevents (tag->string) from escaping HTML sensitive
   chars in content. Used by <script>."
  (r-opts :no-escape true))

(def pad-children
  "Causes (tag->string) to add extra spaces between non-Tag
   child items."
  (r-opts :pad-children true))

;; Standard HTML Tags

(deftags h1 h2 h3 h4 h5 h6 (hr void-element) (br void-element))
(deftags ul ol li dl dt dd)
(deftags b i u s del ins small sup sub code kbd dfn em strong ruby rt rp)
(deftags pre q blockquote cite mark)
(deftags a (img void-element))
(deftags (embed void-element) (object void-element) (param void-element))
(deftags iframe audio video (source void-element) (track void-element) canvas)
(deftags p span div textarea output samp)
(deftags table thead tbody tfoot th tr td caption colgroup (col void-element))
(deftags address article header footer main section aside nav)
(deftags figure figcaption legend)
(deftags form select option optgroup fieldset label input button progress datalist details)
(deftags html title (link void-element) style base head body noscript)

;; Specialized "tags"

(defn !--
  "Ouputs an HTML Comment."
  [& content]
  (Comment. (str-join content)))

(def script
  "Outputs unescaped contents in a <script> tag."
  (tag "script" no-escape))

(def js
  "Outputs unescaped contents in a <script type=\"text/javascript\"> tag."
  (script {:type "text/javascript"}))

(defn import-js
  "Adds a series of <script> tags to import sequence of JavaScript files."
  [& urls]
  (map #(js {:src %}) urls))

(defn import-css
  "Adds a series of <link> tags to import a sequence of CSS files."
  [& urls]
  (map #(link {:rel "stylesheet" :type "text/css" :href %}) urls))

(defn favicon
  "Sets the page's favicon to the given URL."
  ([type url] (link {:rel "shortcut icon" :type type :href url}))
  ([url] (link {:rel "shortcut icon" :href url})))

(def page-meta
  "Creates a <meta> tag. Is also a void element."
  (tag "meta" void-element))

(defn- split-child-items [contents]
  ((juxt filter remove) child-item? (flatten contents)))

(defn- comp-tags [f g]
  (fn [& contents]
    (let [[items other] (split-child-items contents)]
     (select other (map option items)))))

(def drop-down
  "Makes a select box from the given options"
  (comp-tags select option))

(def bullet-list
  "Makes arguments into items in an unordered list."
  (comp-tags ul li))

(def number-list
  "Makes arguments into items in an ordered list."
  (comp-tags ol li))

(defn radio-button
  "Creates a radio button for given param name with given value."
  [param value]
  (input {:name param :id (str param "-" value) :value value :type "radio"}))

(defn radio-group
  "Makes a group of radio buttons for the parameter of the given name
   with the list of options.
   ex: (radio-group \"dayofweek\" \"Monday\" \"mon\" \"Tuesday\" \"tue\" ...)"
  [param & opts]
  (let [[items other] (split-child-items opts)]
    (div
      other
      (map
        (fn [[text value]]
          [(radio-button param value)
           (label {:for (str param "-" value)} text)])
        (partition 2 items)))))

(defn glossary
  "Builds a defintion list (<dl>/<dt>/<dd>) of the given
   name-value pairs, sorted in alphabetical order."
  [& contents]
  (let [[items other] (split-child-items contents)]
    (dl
      other
      (map
        (fn [[term value]] [(dt term) (dd value)])
        (sort-by first (partition 2 items))))))

(defn abbr
  "Creates an abbreviation
   ex: (abbr \"NASA\" \"National Aeronautics and Space Administration\")"
  [short-name full-name]
  (tag "abbr" {:title full-name} short-name))

(def rtl
  "Overrides text direction to be right-to-left."
  (tag "bdo" {:dir "rtl"}))

(def ltr
  "Overrides text direction to be left-to-right."
  (tag "bdo" {:dir "ltr"}))

(def bdi
  "Isolates contained text from surrounding text direction settings."
  (tag "bdi"))

(defn hyperlink
  "Easier function for building a hyperlink.
   ex: (hyperlink \"http://www.google.com\" \"The Google\")"
  [url & content]
  (a {:href url} content))

;; Decorators

(defn color
  "Sets the foreground color for a Tag."
  ([c] (css :color c))
  ([c t] (t (css :color c))))

(def hide
  "Hides a tag using \"display: none\"."
  (css :display "none"))

(def center
  "Centers a tag using \"margin: 0 auto; text-align: center\"."
  (css :margin "0 auto" :text-align "center"))

;; Character Entities

(def nbsp
  "A non-breaking space."
  (literal "&nbsp;"))

(def copyright
  "A copyright (©) symbol."
  (literal "&copy;"))

(def registered
  "A registered trademark (®) symbol."
  (literal "&reg;"))

(def trademark
  "A trademark (™) symbol."
  (literal "&trade;"))

(def euro
  "A Euro (€) symbol."
  (literal "&euro;"))

(def pound
  "A British Pound (£) symbol."
  (literal "&pound;"))

(def cent
  "A U.S. Cent (¢) symbol."
  (literal "&cent;"))

(def yen
  "A Japanese Yen (¥‎) symbol."
  (literal "&yen;"))
