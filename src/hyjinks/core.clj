(ns hyjinks.core
  (:use [clojure.string :only (escape split join capitalize lower-case)]))

;; Forward definitions to resolve circular references

(def tag? nil)
(def extend-tag nil)

;; General helpers

(defn none [pred & xs] (not (apply some pred xs)))

(defn only-if [pred f x] (if (pred x) (f x) x))

(defn unnamed [x] (only-if (partial instance? clojure.lang.Named) name x))

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

(defmacro defrecord-ifn [& record-parts]
  (let [parted-record (partition-by #(= % 'clojure.lang.IFn) record-parts)
        paramses (map (fn [n] (map #(symbol (str "_" %)) (range 0 n))) (range 0 21))]
    (concat
      (list 'defrecord)
      (apply concat (take 2 parted-record))
      (map (fn [params] `(~'invoke [~'this ~@params] (.applyTo ~'this (list ~@params)))) paramses)
      (list `(~'invoke [~'this ~@(last paramses) ~'more] (.applyTo ~'this (concat (list ~@(last paramses)) ~'more))))
      (apply concat (drop 2 parted-record)))))

;; Core types

(defrecord RenderOptions []
  java.lang.Object
  (toString [this]
    (str-join (map (fn [[k v]] (if v [k (if (not (true? v)) v)])) this))))

(defrecord Attrs []
  java.lang.Object
  (toString [this]
    (str-join (map (fn [[k v]] [" " k "=\"" (html-escape v) "\""]) this)))
  clojure.lang.IFn
  (invoke [this] this)
  (invoke [this x] (.applyTo this (list x)))
  (applyTo [this args] (let [t (first args)] (t this))))

(defrecord Css []
  java.lang.Object
  (toString [this]
    (str-join (map (fn [[k v]] ["; " k ": " v]) this) ";"))
  clojure.lang.IFn
  (invoke [this] this)
  (invoke [this x] (.applyTo this (list x)))
  (applyTo [this args] (let [t (first args)] (t this))))

(defrecord-ifn Tag [tag-name attrs css items r-opts]
  java.lang.Object
  (toString [_]
    (let [attrs-with-css (if (empty? css) attrs (assoc attrs :style (str css)))
          escape-child (if (:no-escape r-opts) identity html-escape)
          child-join (if (:pad-children r-opts) str-join-extra-spaces str-join)]
      (str-join
        "<" tag-name attrs-with-css
        (if (and (empty? items) (not (:both-tags r-opts)))
          " />"
          [">" (child-join (map escape-child items)) "</" tag-name ">"]))))
  clojure.lang.IFn
  (applyTo [this args] (apply extend-tag this args)))

(defrecord Literal [s] java.lang.Object (toString [_] s))

;; Define REPL print methods

(defmacro defprint [type] `(defmethod print-method ~type [x# ^java.io.Writer w#] (.write w# (str x#))))

(defprint Attrs)
(defprint Css)
(defprint Tag)
(defprint Literal)
(defprint RenderOptions)

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

(defn assoc-attrs [t & {:as key-vals}] (update-in t [:attrs] (merge key-vals)))

(defn assoc-css [t & {:as key-vals}] (update-in t [:css] (merge key-vals)))

(defn attrs [& {:as key-vals}] (merge empty-attrs key-vals))

(defn css [& {:as key-vals}] (merge empty-css key-vals))

(defn r-opts [& {:as key-vals}] (merge empty-r-opts key-vals))

(defn tag [tag-name & stuff]
  (let [attrs (apply merge empty-attrs (filter attrs-or-map? stuff))
        css (apply merge empty-css (filter css? stuff))
        r-opts (apply merge empty-r-opts (filter r-opts? stuff))
        items (flatten-seq (filter child-item? stuff))]
    (Tag. tag-name attrs css items r-opts)))

(defn extend-tag [t & stuff]
  (if (empty? stuff)
    t
    (let [attrs (apply merge (:attrs t) (filter attrs-or-map? stuff))
          css (apply merge (:css t) (filter css? stuff))
          r-opts (apply merge (:r-opts t) (filter r-opts? stuff))
          items (concat (:items t) (flatten-seq (filter child-item? stuff)))]
      (Tag. (:tag-name t) attrs css items r-opts))))

(defn literal [& content] (Literal. (str-join content)))

;; Declaring rendering options

(defmacro defflag ([sym] `(def ~sym (r-opts ~(keyword sym) true))))

(defflag both-tags)
(defflag no-escape)
(defflag pad-children)

;; Declaring a whole bunch of tags

(defmacro deftag
  ([sym] `(def ~sym (tag ~(str sym))))
  ([sym0 & syms] `(do ~@(map (fn [sym] `(deftag ~sym)) (conj syms sym0)))))

(deftag h1 h2 h3 h4 h5 h6 hr ul ol li dl dt dd)
(deftag b i u s del ins small sup sub pre q cite mark dbo)
(deftag a img hr embed object param iframe audio video)
(deftag p span div nav br canvas textarea blockquote)
(deftag table thead tbody tfoot th tr td caption col colgroup)
(deftag address article header footer main section aside figure figcaption)
(deftag form legend select option optgroup)
(deftag fieldset label input button progress)
(deftag html head title link style base body noscript)

(defn !-- [& content] (literal (str-join "<!-- " content " -->")))

(def script (tag "script" both-tags no-escape))

(def js (script {:language "text/javascript"}))

(def import-js (comp js (partial hash-map :src)))

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

(defmacro defunit [suffix] `(defn ~suffix [~'x] (if (number? ~'x) (str ~'x ~(name suffix)) (unnamed ~'x))))

(defunit px)
(defunit cm)
(defunit em)
(defunit pt)
(defunit deg)
(defunit %)

;; CSS Value Builders

(defmacro defcssval [id & args]
  (let [prepare-arg (fn [arg] (if (.contains (name arg) "angle") `(deg ~arg) `(unnamed ~arg)))
        format-str (str (name id) "(" (join ", " (repeat (count args) "%s")) ")")]
    `(defn ~id [~@args] (format ~format-str ~@(map prepare-arg args)))))

; Used for: color, background-color

(defcssval rgb r g b)
(defcssval rgba r g b a)

; Used for: background, background-image

(defcssval url u)

; Used for: clip

(defcssval rect t r b l)

; Used for: transform

(defcssval matrix a b c d e f)
(defcssval matrix3d a b c d e f g h i j k l m n o p)
(defcssval translate x y)
(defcssval translate3d x y z)
(defcssval translateX x)
(defcssval translateY y)
(defcssval translateZ z)
(defcssval scale x y)
(defcssval scale3d x y z)
(defcssval scaleX x)
(defcssval scaleY y)
(defcssval scaleZ z)
(defcssval rotate angle)
(defcssval rotate3d x y z angle)
(defcssval rotateX angle)
(defcssval rotateY angle)
(defcssval rotateZ angle)
(defcssval skew x-angle y-angle)
(defcssval skewX angle)
(defcssval skewY angle)
(defcssval perspective n)

; Used for: transition-timing-function

(defcssval cubic-bezier a b c d)

; Used for: linear-gradient

(defcssval -webkit-linear-gradient d c1 c2)
(defcssval linear-gradient d c1 c2)

;; Decorators

(defmacro defdecorator [sym arglist body]
  `(defn ~sym (~arglist (css ~@body)) ([~@arglist ~'t] ((~sym ~@arglist) ~'t))))

(defdecorator color [c] (:color c))

(defdecorator transition [x] (:-webkit-transition x :-moz-transition x :transition x))

(defdecorator transition-timing-function [x] (:transition-timing-function x :-webkit-transition-timing-function x))

(defdecorator transition-delay [x] (:transition-delay x :-webkit-transition-delay x))

(defdecorator transition-duration [x] (:transition-duration x :-webkit-transition-duration x))

(defdecorator transition-property [x] (:transition-property x :-moz-transition-property x :-webkit-transition-property x :-o-transition-property x))

(defdecorator background-gradient [d c1 c2] (
  :background-color c1
  :background-image (linear-gradient d c1 c2)
  :background-image (-webkit-linear-gradient d c1 c2)))

(def hide (css :display "none"))

(def center (css :margin "0 auto" :text-align "center"))

(defn transform [& xs]
  (assert (or (< (count xs) 1) (none tag? (butlast xs))))
  (let [l (last xs)
        t (if (tag? l) l)
        xs (if (nil? t) xs (butlast xs))
        x (join " " xs)
        c (css :-webkit-transform x :-moz-transform x :-ms-transform x :-o-transform x :transform x)]
    (if (nil? t) c (c t))))

;; Character Entities

(defmacro defentity
  ([id] `(defentity ~id ~id))
  ([id value] `(def ~id (literal ~(str "&" (name value) ";")))))

(defentity nbsp)
(defentity copyright copy)
(defentity registered reg)
(defentity trademark trade)
(defentity euro)
(defentity pound)
(defentity cent)
(defentity yen)