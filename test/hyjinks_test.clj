(ns hyjinks-test)

(use 'hyjinks)
(use 'clojure.test)

(defn is=str [x y] (is (= (str x) (str y))))

(deftest to-str
	(is=str (p "Content") "<p>Content</p>")
	(is=str (p {:attr :value} "Content") "<p attr=\"value\">Content</p>")
	(is=str (ul-li "A" "B" "C") "<ul><li>A</li><li>B</li><li>C</li></ul>")
	(is=str (p "<Content>") "<p>&lt;Content&gt;</p>")
	(is=str (p (tag* "Content")) "<p><Content /></p>"))