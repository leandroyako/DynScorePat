# DynScore

**(work in progress)** Render Fosc scores (Abjad port to SC) using SuperCollider Patterns.

## Requirements
* Fosc https://github.com/n-armstrong/fosc
* SuperCollider 3.7+
* Lilypond 2.18+

## Install

Open a new document on your SuperCollider IDE and type:

```supercollider
Quarks.install("https://github.com/leandroyako/DynScorePat");
```

## Usage

```supercollider
(
p = Pbind(
	\octave, 5,
	\scale, Scale.lydian,
	\degree, Pwhite(0,6),
	\ctranspose, Prand([-1, 1], inf),
	\root, 2,
	\dur, Prand([1/8, 1/4], 4),
	\dynamic, Pwhite(-6,6),
	\articulation, Pseq(['.','>','!'], inf),
	\notehead, Pseq(["default", "harmonic"], inf),
	\markup, Pseq(["Allegro assai", "", "text"], inf)
);
)

a = DynScore.new;
a.notes('testPattern', p);
a.render;

```

## Contributing

Bug reports and pull requests are welcome on GitHub at
https://github.com/leandroyako/DynScore. This project is intended to be a safe,
welcoming space for collaboration, and contributors are expected to adhere to
the [Contributor Covenant](http://contributor-covenant.org) code of conduct.

## LICENSE

See [LICENSE](LICENSE)

