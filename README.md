[![Build Status](https://travis-ci.org/rkoeninger/hyjinks.svg?branch=master)](https://travis-ci.org/rkoeninger/hyjinks)
[![Clojars Project](https://img.shields.io/clojars/v/hyjinks.svg)](https://clojars.org/hyjinks)

# hyjinks

HTML generation/templating library for Clojure/ClojureScript.

## Concept & Usage

Hyjinks is a set of functions that map directly to html tag names. So the code you use to generate your markup looks just like the markup itself:

``` clojure
(html (head (title "Welcome Page")) (body (h1 "Hello, World!")))
```

``` html
<html><head><title>Welcome Page</title></head><body><h1>Hello, World!</h1></body></html>
```

Except unlike other markup languages that translate to HTML (think ASP, JSP), the markup language and the programming language are the same, so code can be freely intermixed:

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
  :age 61
  :image-url "https://pbs.twimg.com/profile_images/1747118742/Rusty_Shackleford.jpg"})
```

``` html
<div class="personSummary">
  <img src="https://pbs.twimg.com/profile_images/1747118742/Rusty_Shackleford.jpg" />
  <table>
    <tr><td>First Name</td><td>Rusty</td></tr>
    <tr><td>Last Name</td><td>Shackelford</td></tr>
    <tr><td>Age</td><td>61</td></tr>
  </table>
</div>
```
---
<div class="personSummary">
  <img src="https://pbs.twimg.com/profile_images/1747118742/Rusty_Shackleford.jpg" />
  <table>
    <tr><td>First Name</td><td>Rusty</td></tr>
    <tr><td>Last Name</td><td>Shackelford</td></tr>
    <tr><td>Age</td><td>61</td></tr>
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
(color :red (h6 "Hi!"))
```

``` html
<h6 style="color: red">Hi!</h6>
```
---
<h6 style="color: red">Hi!</h6>

---

But I realized that if you wanted to apply several decorators, it would end up looking like this:

``` clojure
(center (color :red (css-class "welcomeBanner" (h1 "Hi!"))))
```

And that could get unwieldy.

At the same time, I wanted to have a general `make-tag` method that would accept the tag name, attributes, CSS properties and child elements and build a tag. But I also thought it would help to allow users to specify them in any order. So the `make-tag` function looks like this:

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
```
... or ...
``` clojure
(div (center) div-contents)
```

## Endless recombinability

So, the beauty of this is that all the tags and decorators are just functions and functions are arbitrarily recomposable. And it's all in the same language (Clojure), so you don't need a new language for each purpose - a language for laying out a document (HTML), a language for styling a document (CSS), a language for programming the page behavior (JavaScript). You can use Clojure(Script) for all of them. You also wouldn't need to define a new, external language to extend or abstract the above languages - HTML -> Haml, CSS -> SASS, JavaScript -> CoffeeScript. The language is written in the language so extending the language can be done in the language can be done without writing a new language.
