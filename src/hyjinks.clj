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

(defn- readable-string [id] (if (keyword? id) (join " " (map capitalize (split (name id) #"-"))) id))

(defn- $lift [& fs] (partial map #(%1 %2) fs))

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

(dorun (map (fn [sym] (eval `(def ~(symbol (lower-case (str sym "?"))) (partial instance? ~sym)))) [
	'Literal 'Tag 'Css]))

(defn attrs? [x] (and (map? x) (not (css? x)) (not (tag? x)) (not (literal? x))))

(defn child-item? [x] (not (or (attrs? x) (css? x))))

(dorun (map (fn [sym] (let [k (keyword sym) sym (symbol (str "assoc-" sym))]
	(eval `(defn ~sym [~'t & ~'kvs] (assoc ~'t ~k (merge (~k ~'t) (apply hash-map ~'kvs))))))) [
		'attrs 'css]))

(defn css [& key-vals] (merge empty-css (apply hash-map key-vals)))

(defn tag [nm & stuff]
	(let [attrs (apply merge {} (filter attrs? stuff))
	      css (apply merge empty-css (filter css? stuff))
	      items (flatten (filter child-item? stuff))]
		(Tag. nm attrs css items)))

;; Declaring a whole bunch of tags

(defn declare-tag [sym] (eval `(defn ~sym [& ~'items] (apply tag ~(str sym) ~'items))))

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

(defn media-source [url type] (tag "source" {:src url :type type}))

(defn page-meta [prop value] (tag "meta" {:name prop :content value}))

;; Higher-order "tags"

(defn bullet-list [& items] (ul (map li (flatten items))))

(defn number-list [& items] (ol (map li (flatten items))))

(defn row-cells [& items] (tr (map td (flatten items))))

(defn table-rows [& rows] (table (map #(row-cells (flatten %)) rows)))

(defn map-table [m] (table (map (comp tr ($lift (comp td readable-string) td)) (sort-by key m))))

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
			([~@params] (css ~@var-args ~@fix-args))
			([~@params ~'t] (assoc-css ~'t ~@var-args ~@fix-args))))))

(dorun (map (partial apply declare-decorator) [
	['hide {:display "none"}]
	['center {:margin "0 auto" :text-align "center"}]
	['color 'color]]))