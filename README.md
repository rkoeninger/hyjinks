# hyjinks

HTML generation/templating library for Clojure/ClojureScript.

More history can be found in [this Gist of mine](https://gist.github.com/rkoeninger/cfe4f2814eb3301cb772).

## Concept & Usage

Hyjinks is a set of functions that map directly to html tag names. So the code you use to generate your markup looks just like the markup itself:

``` clojure
(html (head (title "Welcome Page")) (body (h1 "Hello, World!")))
```

``` html
<html><head><title>Welcome Page</title></head><body><h1>Hello, World!</h1></body></html>
```

Except unlike other markup languages that translae to HTML (think ASP, JSP), the markup language and the programming language are the same, so code can be freely intermixed:

``` clojure
(ol (map li ["Mercury" "Venus" "Earth" "Mars" "Jupiter" "Saturn" "Uranus" "Neptune"]))
```

``` html
<ol>
  <li>Mercury</li>
  <li>Venus</li>
  <li>Earth</li>
  <li>Mars</li>
  <li>Jupiter</li>
  <li>Saturn</li>
  <li>Uranus</li>
  <li>Neptune</li>
</ol>
```
---
<ol>
  <li>Mercury</li>
  <li>Venus</li>
  <li>Earth</li>
  <li>Mars</li>
  <li>Jupiter</li>
  <li>Saturn</li>
  <li>Uranus</li>
  <li>Neptune</li>
</ol>

---

And you can easily define "higher-order" tags:

``` clojure
(defn num-list [& items] (ol (map li items)))

(num-list ["Mercury" "Venus" "Earth" "Mars" "Jupiter" "Saturn" "Uranus" "Neptune"])
```

Or create HTML templates:

``` clojure
(defn person-summary [person]
  (div {:class "personSummary"}
    (img {:src (:image-url person)})
    (table
      (tr (td "First Name") (td (:first-name person)))
      (tr (td "Last Name") (td (:last-name person)))
      (tr (td "Age") (td (:age person))))))
```

Which can be used like this:

``` clojure
(person-summary {
  :first-name "Rusty"
  :last-name "Shackelford"
  :age 42
  :image-url "https://pbs.twimg.com/profile_images/1747118742/Rusty_Shackleford.jpg"})
```

``` html
<div class="personSummary">
  <img src="https://pbs.twimg.com/profile_images/1747118742/Rusty_Shackleford.jpg" />
  <table>
    <tr><td>First Name</td><td>Rusty</td></tr>
    <tr><td>Last Name</td><td>Shackelford</td></tr>
    <tr><td>Age</td><td>42</td></tr>
  </table>
</div>
```
---
<div class="personSummary">
  <img src="https://pbs.twimg.com/profile_images/1747118742/Rusty_Shackleford.jpg" />
  <table>
    <tr><td>First Name</td><td>Rusty</td></tr>
    <tr><td>Last Name</td><td>Shackelford</td></tr>
    <tr><td>Age</td><td>42</td></tr>
  </table>
</div>

---

Or you could get that person info from the database:

``` clojure
(let [person (get-person-from-db "idnumber")]
  (html
    (head (title "Info about " (:first-name person)))
    (body (person-summary person))))
```

Or a whole list of people:

``` clojure
(let [people (get-people-from-db :cool-people)]
  (html
    (head (title "Info about " (count people) " cool people"))
    (body (map person-summary people))))
```

## Decorators

When I was trying to think of a way for applying attributes and ad-hoc styling to tags, my first idea was something like this:

``` clojure
(color :red (h1 "Hi!"))
```

``` html
<h1 style="color: red">Hi!</h1>
```
---
<h1 style="color: red">Hi!</h1>

---

But I realized that if you wanted to apply several decorators, it would end up looking like this:

``` clojure
(center (color :red (css-class "welcomeBanner" (h1 "Hi!"))))
```

And that could get unweildy.

At the same time, I wanted to have a general `make-tag` method that would accept the tag name, attributes, css properties and child elements and build a tag. But I also thought it would help to allow users to specify them in any order. So the `make-tag` function looks like this:

``` clojure
(defn make-tag [name & stuff] ...)
```

Where `stuff` can be any number of attribute dictionaries, css properties and child elements/strings. This allows both of the following to have an equivalent result:

``` clojure
(css-class "specialHeading" (h1 "Hi!"))
```

``` clojure
(h1 (css-class "specialHeading") "Hi!")
```

You could even define it as a "template tag":

``` clojure
(def special-heading (partial h1 (css-class "specialHeading")))

(special-heading "Hi!")
```

And here's the multiple decorator example from above, re-worked:

``` clojure
(h1 "Hi!" (center) (color :red) (css-class "welcomeBanner"))
```

### Ease of use

This also allows one to encapsulate parts of HTML/CSS that are harder to remember or get right into a single decorator:

``` clojure
(defn center [tag] (apply-css tag :margin "0 auto" :text-align "center"))
```

I can never remember how to center a div in CSS. How do you center a div in hyjinks?

``` clojure
(center my-div)

(div (center) div-contents)
```

## Endless recombinability

So, the beauty of this is that all the tags and decorators are just functions and functions are arbitrarily recomposable. And it's all in the same language, making external substitute languages unnecessary.

Haml and SASS are nice, but hyjinks* completely replace them. And you can customize it further however you want as each layer of the library is exposed for you to build on.

And with ClojureScript, one could use this library to apply "templates" on the client side, replacing libraries like Moustache.js, or Angular or Backbone or whatever (I don't even know that much about these, I just think Burn, Javascript! Burn!).

This is why people love Lisp. It can be whatever language you want, just so long as the language you want has a syntax based on s-expressions.

*Ok, so hyjinks isn't anything special. You can really thank Clojure. Or Hickey. Or McCarthy.