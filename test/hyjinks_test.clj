(ns hyjinks-test)

(use 'hyjinks)
(use 'clojure.test)

(defn is=str [x y] (is (= (str x) (str y))))

(deftest to-str
	(is=str (p "Content") "<p>Content</p>")
	(is=str (p {:attr :value} "Content") "<p attr=\"value\">Content</p>")
	(is=str (ul (map li ["A" "B" "C"])) "<ul><li>A</li><li>B</li><li>C</li></ul>")
	(is=str (p "<Content>") "<p>&lt;Content&gt;</p>")
	(is=str (p (tag* "Content")) "<p><Content /></p>")
	(is=str (div (css* :color :red) (br)) "<div style=\"; color: red;\"><br /></div>")
	(is=str (table (hide) (tr (td "A") (td "B"))) "<table style=\"; display: none;\"><tr><td>A</td><td>B</td></tr></table>"))