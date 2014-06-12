(ns hyjinks)

(use '[clojure.string :only (escape split join capitalize lower-case)])

;; General helpers

(defn- str-k [x] (if (keyword? x) (name x) x))

(defn- str-join [& items] (apply str (map str-k (flatten items))))

(defn html-escape [s]
	(if-not (string? s) s
		(escape (str-k s) {
			\< "&lt;"
			\> "&gt;"
			\& "&amp;"
			\" "&quot;"
			\' "&#39;"})))

(defn- readable-string [id]
	(if-not (keyword? id) id
		(join " " (map capitalize (split (name id) #"-")))))

(defn- $lift [& fs] (partial map #(%1 %2) fs))

(defmacro impl-invoke [record]
	(let [parted-record (partition-by #(= % 'clojure.lang.IFn) record)
	      paramses (map (fn [n] (map #(symbol (str "_" %)) (range 0 n))) (range 0 21))]
		(concat
			(apply concat (take 2 parted-record))
			(map (fn [params] `(~'invoke [~'this ~@params] (.applyTo ~'this (list ~@params)))) paramses)
			(list `(~'invoke [~'this ~@(last paramses) ~'more] (.applyTo ~'this (concat (list ~@(last paramses)) ~'more))))
			(apply concat (drop 2 parted-record)))))

(defn none [pred & xs] (not (apply some pred xs)))

;; Forward definitions to resolve circular references

(def extend-tag nil)

;; Core types

(defn- str-attrs [attrs]
	(str-join (map (fn [[k v]] [" " k "=\"" (html-escape v) "\""]) attrs)))

(defrecord Css []
	java.lang.Object
	(toString [this]
		(str-join (map (fn [[k v]] ["; " k ": " v]) this) ";"))
	clojure.lang.IFn
	(invoke [this] this)
	(invoke [this x] (.applyTo this (list x)))
	(applyTo [this args] (let [tag (first args)] (tag this))))

(impl-invoke (defrecord Tag [tag-name attrs css items]
	java.lang.Object
	(toString [_]
		(let [attrs-with-css (if (empty? css) attrs (assoc attrs :style (str css)))]
			(str-join
				"<" tag-name (str-attrs attrs-with-css)
				(if (empty? items)
					" />"
					[">" (map html-escape items) "</" tag-name ">"]))))
	clojure.lang.IFn
	(applyTo [this args] (apply extend-tag this args))))

(defrecord Literal [s] java.lang.Object (toString [_] s))

(impl-invoke (defrecord IFnString [s]
	java.lang.Object (toString [_] s)
	clojure.lang.IFn (applyTo [this args] (join " " (conj args this)))))

(defmethod print-method Css [c ^java.io.Writer w] (.write w (str c)))

(defmethod print-method Tag [t ^java.io.Writer w] (.write w (str t)))

(defmethod print-method Literal [l ^java.io.Writer w] (.write w (str l)))

;; Builder functions

(def empty-css (Css.))

(def literal? (partial instance? Literal))

(def tag? (partial instance? Tag))

(def css? (partial instance? Css))

(defn attrs? [x] (and (map? x) (not (css? x)) (not (tag? x)) (not (literal? x))))

(defn child-item? [x] (not (or (attrs? x) (css? x) (nil? x) (= "" x))))

(defn assoc-attrs [t & key-vals]
	(assoc t :attrs (merge (:attrs t) (apply hash-map key-vals))))

(defn assoc-css [t & key-vals]
	(assoc t :css (merge (:css t) (apply hash-map key-vals))))

(defn css [& key-vals] (merge empty-css (apply hash-map key-vals)))

(defn tag [tag-name & stuff]
	(let [attrs (apply merge {} (filter attrs? stuff))
	      css (apply merge empty-css (filter css? stuff))
	      items (flatten (filter child-item? stuff))]
		(Tag. tag-name attrs css items)))

(defn extend-tag [t & stuff]
	(if (empty? stuff)
		t
		(let [attrs (apply merge (:attrs t) (filter attrs? stuff))
		      css (apply merge (:css t) (filter css? stuff))
		      items (concat (:items t) (flatten (filter child-item? stuff)))]
			(Tag. (:tag-name t) attrs css items))))

(defn literal [& content] (Literal. (str-join content)))

;; Declaring a whole bunch of tags

(defn declare-tag [sym] (eval `(def ~sym (tag ~(str sym)))))

(dorun (map declare-tag [
	'h1 'h2 'h3 'h4 'h5 'h6 'hr
	'b 'i 'u 's 'del 'ins 'small 'sup 'sub 'pre 'q 'cite 'mark 'dbo
	'a 'img 'embed 'object 'param 'iframe 'audio 'video
	'ul 'ol 'li 'dl 'dt 'dd
	'p 'span 'div 'nav 'br 'canvas 'textarea 'blockquote
	'table 'thead 'tbody 'tfoot 'th 'tr 'td 'caption 'col 'colgroup
	'address 'article 'header 'footer 'main 'section 'aside 'figure 'figcaption
	'form 'legend 'select 'option 'optgroup
	'fieldset 'label 'input 'button 'progress
	'html 'head 'title 'link 'style 'script 'base 'body 'noscript]))

(defn !-- [& content] (literal (str-join "<!-- " content " -->")))

(defn media-source [url type] (tag "source" {:src url :type type}))

(defn page-meta [prop value] (tag "meta" {:name prop :content value}))

;; Higher-order "tags"

(defn comp-tag [t u] (fn [& items] (t (map u (flatten items)))))

(def bullet-list (comp-tag ul li))

(def number-list (comp-tag ol li))

(def row-cells (comp-tag tr td))

(defn table-rows [& rows] (table (map #(row-cells (flatten %)) rows)))

(defn map-table [m] (table (map (comp tr ($lift (comp td readable-string) td)) (sort-by key m))))

(defn definitions [term-map]
	(dl (mapcat ($lift dt dd) (sort-by key term-map))))

(defn radio-list [param & opts]
	(mapcat (fn [[t v]] [(label v {:for t}) (input {:id v :value v :name param :type "radio"})]) opts))

;; Decorators

(defmacro defdecorator
	([sym body]
		`(def ~sym (css ~@body)))
	([sym arglist body]
		`(defn ~sym (~arglist (css ~@body)) ([~@arglist ~'t] ((~sym ~@arglist) ~'t)))))

(defdecorator hide (:display "none"))

(defdecorator center (:margin "0 auto" :text-align "center"))

(defdecorator color [c] (:color c))

(defdecorator transition [x] (:-webkit-transition x :-moz-transition x :transition x))

(defdecorator linear-gradient [d c1 c2] (
	:background-color c1
	:background-image (format "-webkit-linear-gradient(%s, %s, %s)" d c1 c2)
	:background-image (format "-linear-gradient(%s, %s, %s)" d c1 c2)))

(defn transform [& xs]
	(assert (or (< (count xs) 1) (none tag? (butlast xs))))
	(let [l (last xs)
	      t (if (tag? l) l)
	      xs (if (nil? t) xs (butlast xs))
	      x (join " " xs)
	      c (css :-webkit-transform x :-moz-transform x :-ms-transform x :-o-transform x :transform x)]
		(if (nil? t) c (c t))))

;; CSS Value Builders

(defn- make-angle [x] (if (number? x) (str x "deg") x))

(defn rotate [angle] (format "rotate(%s)" (str-k (make-angle angle))))

(defn skew [x-angle y-angle] (format "skew(%s, %s)" (str-k (make-angle x-angle)) (str-k (make-angle y-angle))))

;; Character Entities

(def nbsp (literal "&nbsp;"))
(def copyright (literal "&copy;"))
(def registered (literal "&reg;"))
(def trademark (literal "&trade;"))
(def euro (literal "&euro;"))
(def pound (literal "&pound;"))
(def cent (literal "&cent;"))
(def yen (literal "&yen;"))