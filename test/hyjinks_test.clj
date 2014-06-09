(ns hyjinks-test)

(use 'hyjinks)
(use 'clojure.test)

(defn is=str [& xs] (is (apply = (map str xs))))

(deftest to-str
	(is=str
		(p "Content")
		"<p>Content</p>")
	(is=str
		(p {:attr :value} "Content")
		"<p attr=\"value\">Content</p>")
	(is=str
		(ul (map li ["A" "B" "C"]))
		"<ul><li>A</li><li>B</li><li>C</li></ul>")
	(is=str
		(p "<Content>")
		"<p>&lt;Content&gt;</p>")
	(is=str
		(p (literal "<Content>"))
		"<p><Content></p>")
	(is=str
		(p (tag "Content"))
		"<p><Content /></p>")
	(is=str
		(div (css :color :red) (br))
		"<div style=\"; color: red;\"><br /></div>")
	(is=str (p) (p "") (p nil) (p "" nil))
	
	; Tag can just be a constant if it doesn't need arguments
	(is=str (br) br)

	; CSS properties can be constants if they don't need arguments
	(is=str
		(table (hide) (tr (td "A") (td "B")))
		(table hide (tr (td "A") (td "B")))
		"<table style=\"; display: none;\"><tr><td>A</td><td>B</td></tr></table>")
	(is=str
		(map-table {:first-name "Rusty" :last-name "Shackelford"})
		"<table><tr><td>First Name</td><td>Rusty</td></tr><tr><td>Last Name</td><td>Shackelford</td></tr></table>")

	; This test is essentially broken as attributes are sorted
	; by hash function and not in any user-reasonable manner.
	(is=str
		(tag "asd" {:a 4 :b :d} "qwe" {:q :e :f 4} (css :asd "qwe" :dfg :ert) "sdf")
		"<asd style=\"; asd: qwe; dfg: ert;\" q=\"e\" f=\"4\" b=\"d\" a=\"4\">qwesdf</asd>")

	; Unary application should be idempotent
	(is=str
		(div)
		((div))
		(((div)))
		((((div)))))

	; Applying tag as function should be same as extend-tag
	(is=str
		(extend-tag (div (center)) (color :red))
		(div (center) (color :red)))
	(is=str
		((div (center)) (color :red))
		(div (center) (color :red)))
	(def special-div (div {:class "special"} (center)))
	(is=str
		(special-div "Hi")
		(div (center) {:class "special"} "Hi"))

	; Test tag application with many arguments - up to 20
	(is=str ((p) "a" "b" "c" "d" "e" "f" "g" "h" "i" "j") "<p>abcdefghij</p>"))