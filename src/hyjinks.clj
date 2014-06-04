(ns hyjinks)

(use '[clojure.string :only (escape split join)])

;; General helpers

(defn- str-k [x] (if (keyword? x) (name x) x))

(defn- str-join [& items] (apply str (map str-k (flatten items))))

(defn- merge-assoc [m k & key-vals] (assoc m k (merge (k m) (apply hash-map key-vals))))

(defn html-escape [s]
	(if-not (string? s) s
		(escape (str-k s) {
			\< "&lt;"
			\> "&gt;"
			\& "&amp;"
			\" "&quot;"
			\' "&#39;"})))

(defn- capitalize [s] (case (= s "") "" (nil? s) nil true (str (Character/toUpperCase (.charAt s 0)) (.substring s 1))))

(defn- readable-string [id] (join " " (map capitalize (split id #"-"))))

(defn- $ [f x] (f x))

(defn- $lift [& fs] (fn [xs] (map $ fs xs)))

;; Core types

(defrecord Css []
	java.lang.Object
	(toString [this]
		(str-join (map (fn [[k v]] ["; " k ": " v]) this) ";")))

(defn- str-attrs [attrs] (str-join (map (fn [[k v]] [" " k "=\"" (html-escape v) "\""]) attrs)))

(defrecord Tag [tag-name attrs css items]
	java.lang.Object
	(toString [this]
		(let [attrs-with-css (if (empty? css) attrs (assoc attrs :style (str css)))]
			(str-join
				"<" tag-name (str-attrs attrs-with-css)
				(if (empty? items)
					" />"
					[">" (map html-escape items) "</" tag-name ">"])))))

(defmethod print-method Tag [t ^java.io.Writer w] (.write w (str t)))

(defrecord Literal [s] java.lang.Object (toString [_] s))

;; Builder functions

(def empty-css (Css.))

(defn literal? [x] (instance? Literal x))

(defn tag? [x] (instance? Tag x))

(defn css? [x] (instance? Css x))

(defn attrs? [x] (and (map? x) (not (css? x)) (not (tag? x)) (not (literal? x))))

(defn child-item? [x] (not (or (attrs? x) (css? x))))

(defn assoc-attrs [tag & key-vals] (apply merge-assoc tag :attrs key-vals))

(defn assoc-css [tag & key-vals] (apply merge-assoc tag :css key-vals))

(defn new-css [& key-vals] (merge empty-css (apply hash-map key-vals)))

(defn new-tag [nm & stuff]
	(let [attrs (apply merge {} (filter attrs? stuff))
	      css (apply merge empty-css (filter css? stuff))
	      items (flatten (filter child-item? stuff))]
		(Tag. nm attrs css items)))

;; Declaring a whole bunch of tags

(defn declare-tag [sym] (eval `(defn ~sym [& ~'items] (apply new-tag ~(str sym) ~'items))))

(dorun (map declare-tag [
	'h1 'h2 'h3 'h4 'h5 'h6 'hr
	'b 'i 'u 's 'del 'ins 'small 'sup 'sub 'pre 'q 'cite 'mark 'dbo
	'a 'img 'embed 'object 'param 'iframe 'audio 'video
	'ul 'ol 'li 'dl 'dt 'dd
	'p 'span 'div 'nav 'br 'canvas 'textarea 'blockquote
	'table 'thead 'tbody 'tfoot 'th 'tr 'td 'caption 'col 'colgroup
	'address 'article 'header 'footer 'main 'section 'aside 'figure 'figcaption
	'form 'legend 'fieldset 'select 'option 'optgroup 'label 'input 'button 'progress
	'html 'head 'title 'link 'style 'script 'base 'body 'noscript]))

(defn literal [& content] (Literal. (str-join content)))

(defn !-- [& content] (literal (str-join "<!-- " content " -->")))

(defn media-source [url type] (new-tag "source" {:src url :type type}))

(defn page-meta [prop value] (new-tag "meta" {:name prop :content value}))

;; Higher-order "tags"

(defn bullet-list [& items] (ul (map li (flatten items))))

(defn number-list [& items] (ol (map li (flatten items))))

(defn row-cells [& items] (tr (map td (flatten items))))

(defn table-rows [& rows] (table (map #(row-cells (flatten %)) rows)))

(defn map-table [m] (table (map (fn [[k v]] (tr (td (if (keyword? k) (readable-string (name k)) k)) (td v))) (sort-by key m))))

(defn definitions [term-map]
	(dl (mapcat ($lift dt dd) (sort-by key term-map))))

(defn radio-list [param & opts]
	(mapcat (fn [[t v]] [(label v {:for t}) (input {:id v :value v :name param :type "radio"})]) opts))

;; Decorators

(defn declare-decorator [sym & props]
	(let [params (filter symbol? props)
	      var-args (mapcat #(vector (keyword %) (symbol %)) params)
	      fix-args (apply concat (apply merge {} (filter map? props)))]
		(eval `(defn ~sym
			([~@params] (new-css ~@var-args ~@fix-args))
			([~@params ~'tag] (assoc-css ~'tag ~@var-args ~@fix-args))))))

(dorun (map (partial apply declare-decorator) [
	['hide {:display "none"}]
	['center {:margin "0 auto" :text-align "center"}]
	['color 'color]]))