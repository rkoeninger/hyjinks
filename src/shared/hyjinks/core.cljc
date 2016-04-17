(ns hyjinks.core
  (:use [clojure.string :only (escape split join capitalize lower-case)]))

;; Forward definitions to resolve circular references

(declare tag?)
(declare extend-tag)

;; General helpers

(defn none [pred & xs] (not (apply some pred xs)))

(defn only-if [pred f x] (if (pred x) (f x) x))

(defn unnamed [x] (only-if (partial instance? #?(:clj clojure.lang.Named :cljs cljs.core.INamed)) name x))

(defn flatten-seq [& xs] (only-if sequential? flatten xs))

(defn str-join [& items] (apply str (map unnamed (flatten-seq items))))

(defn interposep [sep pred coll]
  (cond
    (empty? coll) (empty coll)
    (= 1 (count coll)) coll
    :else (if (pred (first coll) (second coll))
      (concat [(first coll) sep] (interposep sep pred (rest coll)))
      (concat [(first coll)] (interposep sep pred (rest coll))))))

(defn str-join-extra-spaces [& items]
  (->> items
    flatten-seq
    (map unnamed)
    (interposep " " #(not (or (tag? %1) (tag? %2))))
    (apply str)))

(defn html-escape [x] (only-if string? #(escape % {
  \< "&lt;"
  \> "&gt;"
  \& "&amp;"
  \" "&quot;"
  \' "&#39;"}) x))

#?(:clj (defmacro defrecord-ifn [& record-parts]
  (let [parted-record (partition-by #(= % 'clojure.lang.IFn) record-parts)
        paramses (map (fn [n] (map #(symbol (str "_" %)) (range 0 n))) (range 0 21))]
    (concat
      (list 'defrecord)
      (apply concat (take 2 parted-record))
      (map (fn [params] `(~'invoke [~'this ~@params] (.applyTo ~'this (list ~@params)))) paramses)
      (list `(~'invoke [~'this ~@(last paramses) ~'more] (.applyTo ~'this (concat (list ~@(last paramses)) ~'more))))
      (apply concat (drop 2 parted-record))))))

(defn attr-name [k]
  (case k
    :className "class"
    (lower-case (name k))))

(defn tag->string [{:keys [tag-name attrs css r-opts items]}]
  (let [attrs-with-css (if (empty? css) attrs (assoc attrs :style (str css)))
        escape-child (if (:no-escape r-opts) identity html-escape)
        child-join (if (:pad-children r-opts) str-join-extra-spaces str-join)]
    (str-join
      (if (= tag-name "html") "<!DOCTYPE html>")
      "<" tag-name attrs-with-css
      (if (and (empty? items) (not (:both-tags r-opts)))
        " />"
        [">" (child-join (map escape-child items)) "</" tag-name ">"]))))

;; Core types

(defrecord RenderOptions []
  #?(:clj java.lang.Object
     :cljs Object)
  (toString [this]
    (str-join (map (fn [[k v]] (if v [k (if (not (true? v)) v)])) this))))

(defrecord Attrs []
  #?(:clj java.lang.Object
     :cljs Object)
  (toString [this]
    (str-join (map (fn [[k v]] [" " (attr-name k) "=\"" (html-escape v) "\""]) this)))
  #?@(:clj [
    clojure.lang.IFn
    (invoke [this] this)
    (invoke [this x] (.applyTo this (list x)))
    (applyTo [this args] ((first args) this))]))

#?(:cljs
  (extend-type Attrs
    cljs.core/IFn
    (-invoke
      ([this] this)
      ([this t] (t this)))))

(defrecord Css []
  #?(:clj java.lang.Object
     :cljs Object)
  (toString [this]
    (str-join (map (fn [[k v]] ["; " k ": " v]) this) ";"))
  #?@(:clj [
    clojure.lang.IFn
    (invoke [this] this)
    (invoke [this x] (.applyTo this (list x)))
    (applyTo [this args] ((first args) this))]))

#?(:cljs
  (extend-type Css
    cljs.core/IFn
    (-invoke
      ([this] this)
      ([this t] (t this)))))

(#?(:clj defrecord-ifn :cljs defrecord) Tag [tag-name attrs css items r-opts]
  #?(:clj java.lang.Object
     :cljs Object)
  (toString [this] (tag->string this))
  #?@(:clj [
    clojure.lang.IFn
    (applyTo [this args] (apply extend-tag this args))]))

; #?(:cljs (defmacro invokes [n]
;   (let [args (fn [m] (map #(symbol (str "x" %)) (range m)))]
;   `(~'-invoke
;     ([~'this] ~'this)
;     ~@(map (fn [m] `([~'this ~@(args m)] (~'extend-tag ~'this ~@(args m)))) (range 1 (inc n)))
;     ([~'this ~@(args n) ~'more] (apply extend-tag ~@(args n) ~'more))))))

; TODO: generate -invoke overloads for cljs.core/IFn
#?(:cljs
  (extend-type Tag
    cljs.core/IFn
;    (invokes 20)
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
      ([this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19 more] (apply extend-tag this x0 x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19 more)))))

(defrecord Literal [s]
  #?(:clj java.lang.Object
     :cljs Object)
  (toString [_] s))

;; Define REPL print methods
#?(:clj (defmacro defprint [type] `(defmethod print-method ~type [x# ^java.io.Writer w#] (.write w# (str x#)))))
#?(:clj (defprint Attrs))
#?(:clj (defprint Css))
#?(:clj (defprint Tag))
#?(:clj (defprint Literal))
#?(:clj (defprint RenderOptions))

;; Builder functions

(def empty-attrs (Attrs.))

(def empty-css (Css.))

(def empty-r-opts (RenderOptions.))

(defn literal? [x] (instance? Literal x))

(defn tag? [x] (instance? Tag x))

(defn css? [x] (instance? Css x))

(defn attrs? [x] (instance? Attrs x))

(defn r-opts? [x] (instance? RenderOptions x))

(defn attrs-or-map? [x] (or (attrs? x) (and (map? x) (not (record? x)))))

(defn child-item? [x] (not (or (attrs-or-map? x) (css? x) (r-opts? x) (nil? x) (= "" x))))

(defn assoc-attrs [t & {:as key-vals}] (update-in t [:attrs] (merge key-vals))) ; TODO merge :className

(defn assoc-css [t & {:as key-vals}] (update-in t [:css] (merge key-vals)))

(defn attrs [& {:as key-vals}] (merge empty-attrs key-vals))

(defn css [& {:as key-vals}] (merge empty-css key-vals))

(defn r-opts [& {:as key-vals}] (merge empty-r-opts key-vals))

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
        attrs (apply merge empty-attrs
                           (if id {:id id})
                           (if (seq class-names) {:className (join " " class-names)})
                           (filter attrs-or-map? stuff))
        css    (apply merge empty-css (filter css? stuff))
        r-opts (apply merge empty-r-opts (filter r-opts? stuff))
        items  (flatten-seq (filter child-item? stuff))]
    (Tag. tag-name attrs css items r-opts)))

(defn extend-tag [{:keys [tag-name attrs css r-opts items] :as t} & stuff]
  (if (empty? stuff)
    t
    (let [attrs  (apply merge attrs (filter attrs-or-map? stuff))
          css    (apply merge css (filter css? stuff))
          r-opts (apply merge r-opts (filter r-opts? stuff))
          items  (concat items (flatten-seq (filter child-item? stuff)))]
      (Tag. tag-name attrs css items r-opts))))

(defn literal [& content] (Literal. (str-join content)))

;; Declaring rendering options

; TODO: fix this? remove macro? cljsbuild didn't like defflag for some reason

;(defmacro defflag [sym] `(def ~sym (r-opts ~(keyword sym) true)))

;(defflag both-tags)
;(defflag no-escape)
;(defflag pad-children)

(def both-tags (r-opts :both-tags true))
(def no-escape (r-opts :no-escape true))
(def pad-children (r-opts :pad-children true))

;; Declaring a whole bunch of tags

; TODO - restore macro?

(def h1 (tag "h1"))
(def h2 (tag "h2"))
(def h3 (tag "h3"))
(def h4 (tag "h4"))
(def h5 (tag "h5"))
(def h6 (tag "h6"))
(def hr (tag "hr"))
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
(def img (tag "img"))
(def embed (tag "embed"))
(def object (tag "object"))
(def param (tag "param"))
(def iframe (tag "iframe"))
(def audio (tag "audio"))
(def video (tag "video"))
(def p (tag "p"))
(def span (tag "span"))
(def div (tag "div"))
(def nav (tag "nav"))
(def br (tag "br"))
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
(def col (tag "col"))
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
(def input (tag "input"))
(def button (tag "button"))
(def progress (tag "progress"))
(def html (tag "html"))
(def head (tag "head"))
(def title (tag "title"))
(def link (tag "link"))
(def style (tag "style"))
(def base (tag "base"))
(def body (tag "body"))
(def noscript (tag "noscript"))

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

(defn comp-tag [t u] (fn [& items] (t (map u (flatten-seq items)))))

(def bullet-list (comp-tag ul li))

(def number-list (comp-tag ol li))

(def row-cells (comp-tag tr td))

(defn table-rows [& rows] (table (map (comp row-cells flatten-seq) rows)))

(defn radio-list [param & opts] (mapcat (fn [[text value]] [(radio param value) (label text {:for value})]) (partition 2 opts)))

(defn vertical-radio-list [param & opts] (apply table-rows (partition 2 (apply radio-list param opts))))

;; CSS Units

; TODO: restore this macro?

;(defmacro defunit [suffix] `(defn ~suffix [~'x] (if (number? ~'x) (str ~'x ~(name suffix)) (unnamed ~'x))))

;(defunit px)
;(defunit cm)
;(defunit em)
;(defunit pt)
;(defunit deg)
;(defunit %)

(defn px [x] (if (number? x) (str x "px")) (unnamed x))
(defn cm [x] (if (number? x) (str x "cm")) (unnamed x))
(defn em [x] (if (number? x) (str x "em")) (unnamed x))
(defn pt [x] (if (number? x) (str x "pt")) (unnamed x))
(defn deg [x] (if (number? x) (str x "deg")) (unnamed x))
(defn % [x] (if (number? x) (str x "%")) (unnamed x))

;; CSS Value Builders

; TODO - restore all this stuff?

;(defmacro defcssval [id & args]
;  (let [prepare-arg (fn [arg] (if (.contains (name arg) "angle") `(deg ~arg) `(unnamed ~arg)))
;        format-str (str (name id) "(" (join ", " (repeat (count args) "%s")) ")")]
;    `(defn ~id [~@args] (format ~format-str ~@(map prepare-arg args)))))

; Used for: color, background-color

;(defcssval rgb r g b)
;(defcssval rgba r g b a)

; Used for: background, background-image

;(defcssval url u)

; Used for: clip

;(defcssval rect t r b l)

; Used for: transform

; (defcssval matrix a b c d e f)
; (defcssval matrix3d a b c d e f g h i j k l m n o p)
; (defcssval translate x y)
; (defcssval translate3d x y z)
; (defcssval translateX x)
; (defcssval translateY y)
; (defcssval translateZ z)
; (defcssval scale x y)
; (defcssval scale3d x y z)
; (defcssval scaleX x)
; (defcssval scaleY y)
; (defcssval scaleZ z)
; (defcssval rotate angle)
; (defcssval rotate3d x y z angle)
; (defcssval rotateX angle)
; (defcssval rotateY angle)
; (defcssval rotateZ angle)
; (defcssval skew x-angle y-angle)
; (defcssval skewX angle)
; (defcssval skewY angle)
; (defcssval perspective n)

; Used for: transition-timing-function

;(defcssval cubic-bezier a b c d)

; Used for: linear-gradient

;(defcssval -webkit-linear-gradient d c1 c2)
;(defcssval linear-gradient d c1 c2)

;; Decorators

;(defmacro defdecorator [sym arglist body]
  ;`(defn ~sym (~arglist (css ~@body)) ([~@arglist ~'t] ((~sym ~@arglist) ~'t))))

;(defdecorator color [c] (:color c))

(defn color
  ([c] (css :color c))
  ([c t] (t (css :color c))))

; (defdecorator transition [x] (:-webkit-transition x :-moz-transition x :transition x))

; (defdecorator transition-timing-function [x] (:transition-timing-function x :-webkit-transition-timing-function x))

; (defdecorator transition-delay [x] (:transition-delay x :-webkit-transition-delay x))

; (defdecorator transition-duration [x] (:transition-duration x :-webkit-transition-duration x))

; (defdecorator transition-property [x] (:transition-property x :-moz-transition-property x :-webkit-transition-property x :-o-transition-property x))

; (defdecorator background-gradient [d c1 c2] (
;   :background-color c1
;   :background-image (linear-gradient d c1 c2)
;   :background-image (-webkit-linear-gradient d c1 c2)))

(def hide (css :display "none"))

(def center (css :margin "0 auto" :text-align "center"))

; (defn transform [& xs]
;   (assert (or (< (count xs) 1) (none tag? (butlast xs))))
;   (let [l (last xs)
;         t (if (tag? l) l)
;         xs (if (nil? t) xs (butlast xs))
;         x (join " " xs)
;         c (css :-webkit-transform x :-moz-transform x :-ms-transform x :-o-transform x :transform x)]
;     (if (nil? t) c (c t))))

;; Character Entities

; (defmacro defentity
;   ([id] `(defentity ~id ~id))
;   ([id value] `(def ~id (literal ~(str "&" (name value) ";")))))

; (defentity nbsp)
; (defentity copyright copy)
; (defentity registered reg)
; (defentity trademark trade)
; (defentity euro)
; (defentity pound)
; (defentity cent)
; (defentity yen)
