(ns hyjinks.core
  (:use [clojure.string :only (escape split join capitalize lower-case trim)]))

;; Forward definitions to resolve circular references

(declare tag?)
(declare extend-tag)

;; General helpers

(defn- prune-map [m]
  (let [rem? #(or (nil? %) (= "" %) (and (coll? %) (empty? %)))]
    (apply dissoc m (map first (filter (comp rem? second) m)))))

(defn- str-join [& items] (join "" (flatten items)))

(defn- interposep [sep pred coll]
  (cond
    (empty? coll) (empty coll)
    (= 1 (count coll)) coll
    :else (if (pred (first coll) (second coll))
      (concat [(first coll) sep] (interposep sep pred (rest coll)))
      (concat [(first coll)] (interposep sep pred (rest coll))))))

(defn- str-join-extra-spaces [& items]
  (->> items
    flatten
    (interposep " " #(not (or (tag? %1) (tag? %2))))
    (apply str)))

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

(defn attr-name [k]
  (case k
    :className "class"
    (lower-case (name k))))

(defn tag->string [{:keys [tag-name attrs css r-opts items]}]
  (let [{:keys [className]} attrs
        attrs (if (sequential? className) (assoc attrs :className (clojure.string/join " " className)) attrs)
        attrs+css (if (empty? css) attrs (assoc attrs :style (str css)))
        escape-child (if (:no-escape r-opts) identity html-escape)
        child-join (if (:pad-children r-opts) str-join-extra-spaces str-join)]
    (str-join
      (if (= tag-name "html") "<!DOCTYPE html>")
      "<" tag-name attrs+css
      (if (and (empty? items) (not (:both-tags r-opts)))
        " />"
        [">" (child-join (map escape-child items)) "</" tag-name ">"]))))

;; Core types

(defrecord Literal [s]
  #?(:clj java.lang.Object :cljs Object)
  (toString [_] s))

(defrecord RenderOptions []
  #?(:clj java.lang.Object :cljs Object)
  (toString [this]
    (str-join (filter (fn [[k v]] (if v [k v])) this))))

(defrecord Attrs []
  #?(:clj java.lang.Object :cljs Object)
  (toString [this]
    (str-join (map (fn [[k v]] [" " (attr-name k) "=\"" (html-escape v) "\""]) this)))
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

(defrecord Tag [tag-name attrs css items r-opts]
  #?(:clj java.lang.Object :cljs Object)
  (toString [this] (tag->string this))
  #?@(:clj [
  clojure.lang.IFn
  (invoke [this] this)
  (invoke [this x0] (extend-tag this x0))
  (invoke [this x0 x1] (extend-tag this x0 x1))
  (invoke [this x0 x1 x2] (extend-tag this x0 x1 x2))
  (invoke [this x0 x1 x2 x3] (extend-tag this x0 x1 x2 x3))
  (invoke [this x0 x1 x2 x3 x4] (extend-tag this x0 x1 x2 x3 x4))
  (invoke [this x0 x1 x2 x3 x4 x5] (extend-tag this x0 x1 x2 x3 x4 x5))
  (invoke [this x0 x1 x2 x3 x4 x5 x6] (extend-tag this x0 x1 x2 x3 x4 x5 x6))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19))
  (invoke [this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19 more] (apply extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19 more))
  (applyTo [this args] (apply extend-tag this args))]))

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
  (extend-type Tag
    cljs.core/IFn
    (-invoke
      ([this] this)
      ([this x0] (extend-tag this x0))
      ([this x0 x1] (extend-tag this x0 x1))
      ([this x0 x1 x2] (extend-tag this x0 x1 x2))
      ([this x0 x1 x2 x3] (extend-tag this x0 x1 x2 x3))
      ([this x0 x1 x2 x3 x4] (extend-tag this x0 x1 x2 x3 x4))
      ([this x0 x1 x2 x3 x4 x5] (extend-tag this x0 x1 x2 x3 x4 x5))
      ([this x0 x1 x2 x3 x4 x5 x6] (extend-tag this x0 x1 x2 x3 x4 x5 x6))
      ([this x0 x1 x2 x3 x4 x5 x6 x7] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19] (extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19))
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19 more] (apply extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19 more))))]))

;; Builder functions

(defn literal? [x] (instance? Literal x))

(defn tag? [x] (instance? Tag x))

(defn css? [x] (instance? Css x))

(defn attrs? [x] (instance? Attrs x))

(defn r-opts? [x] (instance? RenderOptions x))

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
        cs0 (join-class-names (:className xs))
        cs1 (join-class-names (:className ys))]
    (assoc m :className
      (if (and cs0 cs1)
        (str cs0 " " cs1)
        (join-class-names (:className m))))))

(defn- extend-attrs [attrs more] (prune-map (reduce merge-attrs attrs more)))

(defn- extend-css [css more] (prune-map (apply merge css more)))

(defn- extend-r-opts [r-opts more] (prune-map (apply merge r-opts more)))

(defn- extend-items [items more] (vec (concat items more)))

(defn attrs [& {:as key-vals}] (extend-attrs (Attrs.) key-vals))

(defn css [& {:as key-vals}] (extend-css (Css.) key-vals))

(defn- r-opts [& {:as key-vals}] (extend-r-opts (RenderOptions.) key-vals))

(defn- starts-with [prefix s] (= prefix (subs s 0 (#?(:clj .length :cljs .-length) prefix))))

(defn- indexOf [s sub from-index]
  (let [i (.indexOf s sub from-index)]
    (if (neg? i) nil i)))

(defn- split-selector [s]
  (let [i (indexOf s "." 1)
        j (indexOf s "#" 1)
        k (cond
            (not (or i j)) nil
            (not i) j
            (not j) i
            (< j i) j
            (< i j) i)]
    (if k
      (cons (subs s 0 k) (split-selector (subs s k)))
      (cons s nil))))

(defn- parse-selector [selector]
  (let [selector-parts (split-selector (name selector))
        tag-name       (first selector-parts)
        id-clause      (first (filter (partial starts-with "#") selector-parts))
        class-clauses  (filter (partial starts-with ".") selector-parts)]
    {:tag-name    (first selector-parts)
     :id          (if id-clause (subs id-clause 1))
     :class-names (mapv #(subs % 1) class-clauses)}))

(defn tag [selector & stuff]
  (let [{:keys [tag-name id class-names]} (parse-selector selector)
        attrs  (extend-attrs
                 (Attrs.)
                 (conj
                  (filter attrs-or-map? stuff)
                  (if id {:id id})
                  (if (seq class-names) {:className class-names})))
        css    (extend-css    (Css.)           (filter css? stuff))
        r-opts (extend-r-opts (RenderOptions.) (filter r-opts? stuff))
        items  (extend-items  []               (filter child-item? stuff))]
    (Tag. tag-name attrs css items r-opts)))

(defn extend-tag [{:keys [tag-name attrs css r-opts items] :as t} & stuff]
  (if (empty? stuff)
    t
    (let [attrs  (extend-attrs  attrs  (filter attrs-or-map? stuff))
          css    (extend-css    css    (filter css? stuff))
          r-opts (extend-r-opts r-opts (filter r-opts? stuff))
          items  (extend-items  items  (filter child-item? stuff))]
      (Tag. tag-name attrs css items r-opts))))

(defn literal [& content] (Literal. (str-join content)))

;; Declaring rendering options

(def void-element (r-opts :void-element true))
(def self-close (r-opts :self-close true))
(def both-tags (r-opts :both-tags true))
(def no-escape (r-opts :no-escape true))
(def pad-children (r-opts :pad-children true))

;; Declaring a whole bunch of tags

(def h1 (tag "h1"))
(def h2 (tag "h2"))
(def h3 (tag "h3"))
(def h4 (tag "h4"))
(def h5 (tag "h5"))
(def h6 (tag "h6"))
(def hr (tag "hr" void-element))
(def ul (tag "ul"))
(def ol (tag "ol"))
(def li (tag "li"))
(def dl (tag "dl"))
(def dt (tag "dt"))
(def dd (tag "dd"))
(def b (tag "b"))
(def i (tag "i"))
(def u (tag "u"))
(def s (tag "s"))
(def del (tag "del"))
(def ins (tag "ins"))
(def small (tag "small"))
(def sup (tag "sup"))
(def sub (tag "sub"))
(def pre (tag "pre"))
(def q (tag "q"))
(def blockquote (tag "blockquote"))
(def cite (tag "cite"))
(def mark (tag "mark"))
(def dbo (tag "dbo"))
(def a (tag "a"))
(def img (tag "img" void-element))
(def embed (tag "embed" void-element))
(def object (tag "object" void-element))
(def param (tag "param" void-element))
(def iframe (tag "iframe"))
(def audio (tag "audio"))
(def video (tag "video"))
(def p (tag "p"))
(def span (tag "span"))
(def div (tag "div"))
(def nav (tag "nav"))
(def br (tag "br" void-element))
(def canvas (tag "canvas"))
(def textarea (tag "textarea"))
(def table (tag "table"))
(def thead (tag "thead"))
(def tbody (tag "tbody"))
(def tfoot (tag "tfoot"))
(def th (tag "th"))
(def tr (tag "tr"))
(def td (tag "td"))
(def caption (tag "caption"))
(def col (tag "col" void-element))
(def colgroup (tag "colgroup"))
(def address (tag "address"))
(def article (tag "article"))
(def header (tag "header"))
(def footer (tag "footer"))
(def main (tag "main"))
(def section (tag "section"))
(def aside (tag "aside"))
(def figure (tag "figure"))
(def figcaption (tag "figcaption"))
(def form (tag "form"))
(def legend (tag "legend"))
(def select (tag "select"))
(def option (tag "option"))
(def optgroup (tag "optgroup"))
(def fieldset (tag "fieldset"))
(def label (tag "label"))
(def input (tag "input" void-element))
(def button (tag "button"))
(def progress (tag "progress"))
(def html (tag "html"))
(def head (tag "head"))
(def title (tag "title"))
(def link (tag "link" void-element))
(def style (tag "style"))
(def base (tag "base" void-element))
(def body (tag "body"))
(def noscript (tag "noscript"))

;; Specialized tags

(defn !-- [& content] (literal (str-join "<!-- " content " -->")))

(def script (tag "script" both-tags no-escape))

(def js (script {:type "text/javascript"}))

(defn import-js [& urls] (map #(js {:src %}) urls))

(defn import-css [& urls] (map #(link {:rel "stylesheet" :type "text/css" :href %}) urls))

(defn favicon
  ([type url] (link {:rel "shortcut icon" :type type :href url}))
  ([url] (link {:rel "shortcut icon" :href url})))

(defn media-source [url type] (tag "source" {:src url :type type}))

(defn page-meta [prop value] (tag "meta" {:name prop :content value}))

(defn radio [param value] (input {:name param :id value :value value :type "radio"}))

;; Higher-order "tags"

(defn comp-tag [t u] (fn [& items] (t (map u (flatten items)))))

(def bullet-list (comp-tag ul li))

(def number-list (comp-tag ol li))

(def row-cells (comp-tag tr td))

(defn radio-list [param & opts] (mapcat (fn [[text value]] [(radio param value) (label text {:for value})]) (partition 2 opts)))

;; Decorators

(defn color
  ([c] (css :color c))
  ([c t] (t (css :color c))))

(def hide (css :display "none"))

(def center (css :margin "0 auto" :text-align "center"))

;; Character Entities

(def nbsp (literal "&nbsp;"))
(def copyright (literal "&copy;"))
(def registered (literal "&reg;"))
(def trademark (literal "&trade;"))
(def euro (literal "&euro;"))
(def pound (literal "&pound;"))
(def cent (literal "&cent;"))
(def yen (literal "&yen;"))
