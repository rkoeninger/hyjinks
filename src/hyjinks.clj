(ns hyjinks)

(use '[clojure.string :only (escape split join capitalize lower-case)])

;; General helpers

(defn unnamed [x] (if (instance? clojure.lang.Named x) (name x) x))

(defn str-join [& items] (apply str (map unnamed (flatten items))))

(defn html-escape [s]
	(if-not (string? s) s
		(escape (unnamed s) {
			\< "&lt;"
			\> "&gt;"
			\& "&amp;"
			\" "&quot;"
			\' "&#39;"})))

(defmacro defrecord-ifn [& record-parts]
	(let [parted-record (partition-by #(= % 'clojure.lang.IFn) record-parts)
	      paramses (map (fn [n] (map #(symbol (str "_" %)) (range 0 n))) (range 0 21))]
		(concat
			(list 'defrecord)
			(apply concat (take 2 parted-record))
			(map (fn [params] `(~'invoke [~'this ~@params] (.applyTo ~'this (list ~@params)))) paramses)
			(list `(~'invoke [~'this ~@(last paramses) ~'more] (.applyTo ~'this (concat (list ~@(last paramses)) ~'more))))
			(apply concat (drop 2 parted-record)))))

(def record? (partial instance? clojure.lang.IRecord))

(defn none [pred & xs] (not (apply some pred xs)))

;; Forward declarations to resolve circular references

(declare extend-tag nil)

;; Core types

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

(defrecord-ifn Tag [tag-name attrs css items]
	java.lang.Object
	(toString [_]
		(let [attrs-with-css (if (empty? css) attrs (assoc attrs :style (str css)))]
			(str-join
				"<" tag-name attrs-with-css
				(if (empty? items)
					" />"
					[">" (map html-escape items) "</" tag-name ">"]))))
	clojure.lang.IFn
	(applyTo [this args] (apply extend-tag this args)))

(defrecord Literal [s] java.lang.Object (toString [_] s))

;; Define REPL print methods

(defmacro defprint [type] `(defmethod print-method ~type [x# ^java.io.Writer w#] (.write w# (str x#))))

(defprint Attrs)
(defprint Css)
(defprint Tag)
(defprint Literal)

;; Builder functions

(def empty-attrs (Attrs.))

(def empty-css (Css.))

(def literal? (partial instance? Literal))

(def tag? (partial instance? Tag))

(def css? (partial instance? Css))

(def attrs? (partial instance? Attrs))

(def attrs-or-map? (some-fn attrs? (every-pred map? (complement record?))))

(def child-item? (complement (some-fn attrs-or-map? css? nil? empty?)))

(defn assoc-attrs [t & key-vals]
	(assoc t :attrs (merge (:attrs t) (apply hash-map key-vals))))

(defn assoc-css [t & key-vals]
	(assoc t :css (merge (:css t) (apply hash-map key-vals))))

(defn attrs [& key-vals] (merge empty-attrs (apply hash-map key-vals)))

(defn css [& key-vals] (merge empty-css (apply hash-map key-vals)))

(defn tag [tag-name & stuff]
	(let [attrs (apply merge empty-attrs (filter attrs-or-map? stuff))
	      css (apply merge empty-css (filter css? stuff))
	      items (flatten (filter child-item? stuff))]
		(Tag. tag-name attrs css items)))

(defn extend-tag [t & stuff]
	(if (empty? stuff)
		t
		(let [attrs (apply merge (:attrs t) (filter attrs-or-map? stuff))
		      css (apply merge (:css t) (filter css? stuff))
		      items (concat (:items t) (flatten (filter child-item? stuff)))]
			(Tag. (:tag-name t) attrs css items))))

(defn literal [& content] (Literal. (str-join content)))

;; Declaring a whole bunch of tags

(defmacro deftag
	([sym] `(def ~sym (tag ~(str sym))))
	([sym0 & syms] `(do ~@(map (fn [sym] `(deftag ~sym)) (conj syms sym0)))))

(deftag h1 h2 h3 h4 h5 h6 hr)
(deftag b i u s del ins small sup sub pre q cite mark dbo)
(deftag a img hr embed object param iframe audio video)
(deftag ul ol li dl dt dd)
(deftag p span div nav br canvas textarea blockquote)
(deftag table thead tbody tfoot th tr td caption col colgroup)
(deftag address article header footer main section aside figure figcaption)
(deftag form legend select option optgroup)
(deftag fieldset label input button progress)
(deftag html head title link style script base body noscript)

(defn !-- [& content] (literal (str-join "<!-- " content " -->")))

(defn media-source [url type] (tag "source" {:src url :type type}))

(defn page-meta [prop value] (tag "meta" {:name prop :content value}))

;; Higher-order "tags"

(defn comp-tag [t u] (fn [& items] (t (map u (flatten items)))))

(def bullet-list (comp-tag ul li))

(def number-list (comp-tag ol li))

(def row-cells (comp-tag tr td))

(defn table-rows [& rows] (table (map #(row-cells (flatten %)) rows)))

(defn radio-list [param & opts]
	(mapcat (fn [[t v]] [(label v {:for t}) (input {:id v :value v :name param :type "radio"})]) opts))

;; CSS Units

(defmacro defunit [suffix] `(defn ~suffix [~'x] (if (number? ~'x) (str ~'x ~(name suffix)) (unnamed ~'x))))

(defunit px)
(defunit deg)
(defunit %)

;; CSS Value Builders

(defmacro defcssval [id & args]
	(let [prepare-arg (fn [arg] (if (.contains (name arg) "angle") `(deg ~arg) `(unnamed ~arg)))
	      format-str (str (name id) "(" (join ", " (repeat (count args) "%s")) ")")]
		`(defn ~id [~@args] (format ~format-str ~@(map prepare-arg args)))))

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

; linear-gradient
(defdecorator gradient [d c1 c2] (
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