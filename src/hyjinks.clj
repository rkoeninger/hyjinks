(ns hyjinks)

(use '[clojure.string :only (escape)])

;; General helpers

(defn- html-escape [s]
	(if-not (string? s) s (escape s {
		\< "&lt;"
		\> "&gt;"
		\& "&amp;"
		\" "&quot;"
		\' "&#39;"})))

(defn- str-k [s] (if (keyword? s) (name s) s))

(defn- str-join [& items] (apply str (map str-k (flatten items))))

(defn- merge-assoc [m k & key-vals] (assoc m k (merge (k m) (apply hash-map key-vals))))

;; Core types

(defn- str-attrs [attrs] (str-join (map (fn [[k v]] [" " k "=\"" v "\""]) attrs)))

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

(defrecord Css []
	java.lang.Object
	(toString [this]
		(str-join (map (fn [[k v]] ["; " k ": " v]) this) ";")))

;; Builder functions

(def empty-css (Css.))

(defn attrs? [x] (and (map? x) (not (instance? Css x)) (not (instance? Tag x))))

(defn css? [x] (instance? Css x))

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
	'h1 'h2 'h3 'h4 'h5 'h6
	'b 'i 'u 's 'del 'ins 'small 'sup 'sub 'pre 'q 'cite 'mark 'dbo
	'a 'img 'embed 'object 'param 'iframe
	'ul 'ol 'li 'dl 'dt 'dd
	'p 'span 'div 'nav 'br 'canvas 'textarea 'blockquote
	'table 'thead 'tbody 'tfoot 'th 'tr 'td 'caption 'colgroup 'col
	'address 'article 'header 'footer 'main 'section 'aside 'figure 'figcaption
	'form 'legend 'fieldset 'select 'label 'input 'button 'progress
	'html 'head 'title 'style 'base 'body 'noscript]))

;; Tags with specific features

(defn !-- [& content] (str-join "<!-- " content " -->"))

(defn audio [controls & items] (new-tag "audio" {:controls controls} items))

(defn video [controls & items] (new-tag "video" {:controls controls} items))

(defn media-source [url type] (new-tag "source" {:src url :type type}))

(defn track [url kind lang label] (new-tag "track" {:src url :kind kind :srclang lang :label label}))

(defn abbr [title & items] (new-tag "abbr" {:title title} items))

(defn datetime
	([content] (new-tag "time" content))
	([timestring content] (new-tag "time" {:datetime timestring} content)))

(defn option
	([value] (new-tag "option" {:value value} value))
	([label value] (new-tag "option" {:value value} label)))

(defn optgroup [label & opts] (new-tag "optgroup" {:label label} opts))

(defn page-meta [prop value] (new-tag "meta" {:name prop :content value}))

(defn page-link [rel url] (new-tag "link" {:rel rel :href url}))

(defn script
	([syntax] (new-tag "script" [syntax]))
	([lang syntax] (new-tag "script" {:language lang} syntax)))

(defn import-script
	([url] (new-tag "script" {:src url :charset "utf-8"} [""]))
	([lang url] (new-tag "script" {:language lang :src url :charset "utf-8"} [""])))

;; Higher-order "tags"

(defn ul-li [& items] (ul (map li (flatten items))))

(defn ol-li [& items] (ol (map li (flatten items))))

(defn tr-td [& items] (tr (map td (flatten items))))

(defn table-tr-td [& rows] (table (map (fn [row] (tr-td (flatten row))) rows)))

(defn dl-dt-dd [term-map]
	(dl (mapcat (fn [[t d]] [(dt t) (dd d)]) (sort-by key term-map))))

(defn radio-list [param & opts]
	(mapcat (fn [[t v]] [(label v {:for t}) (input {:id v :value v :name param :type "radio"})]) opts))

;; Attribute/CSS Decorators

(defn declare-decorator [dec-name & props]
	(let [params (filter symbol? props)
	      args (apply concat (map (fn [x] [(keyword x) (symbol x)]) params))
	      fixed (apply concat (apply merge {} (filter map? props)))]
		(eval `(defn ~dec-name
			([~@params] (new-css ~@args ~@fixed))
			([~@params ~'tag] (assoc-css ~'tag ~@args ~@fixed))))))

(dorun (map (partial apply declare-decorator) [
	['hide {:display "none"}]
	['center {:margin "0 auto" :text-align "center"}]
	['color 'color]]))