(ns hyjinks-test)

(use 'hyjinks)
(use 'clojure.test)

(defn is=str [x y] (is (= (str x) (str y))))

(deftest to-str
	(is=str (p "Content") "<p>Content</p>")
	(is=str (p {:attr :value} "Content") "<p attr=\"value\">Content</p>")
	(is=str (ul (map li ["A" "B" "C"])) "<ul><li>A</li><li>B</li><li>C</li></ul>")
	(is=str (p "<Content>") "<p>&lt;Content&gt;</p>")
	(is=str (p (new-tag "Content")) "<p><Content /></p>")
	(is=str (div (new-css :color :red) (br)) "<div style=\"; color: red;\"><br /></div>")
	(is=str
		(table (hide) (tr (td "A") (td "B")))
		"<table style=\"; display: none;\"><tr><td>A</td><td>B</td></tr></table>")

	; This test is essentially broken as attributes are sorted
	; by hash function and not in any user-reasonable manner.
	(is=str
		(new-tag "asd" {:a 4 :b :d} "qwe" {:q :e :f 4} (new-css :asd "qwe" :dfg :ert) "sdf")
		"<asd style=\"; asd: qwe; dfg: ert;\" q=\"e\" f=\"4\" b=\"d\" a=\"4\">qwesdf</asd>"))