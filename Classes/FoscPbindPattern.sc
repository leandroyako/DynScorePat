FoscPbindPattern {
	var <score, <pbind, <type;

	classvar patternBuilder;

	*new { |score, pbind, type=\note|
		^super.newCopyArgs(score, pbind, type);
	}

	*initClass {
		patternBuilder = this.prPatternBuilder;
	}

	play {
		(patternBuilder <> (abjad: type, score: score) <> pbind).play;
	}

	render {
		score.render(this.pbind);
	}

	preview {
		score.preview(this.pbind);
	}

	*prPatternBuilder {
		^Pbind(
			\delta, 0,
			\amp, 0,
			\finish, {|ev|
				var selectedKeys;

				selectedKeys = ev.reject {
					|item, key|
					['delta', 'path', 'score', 'abjad', 'finish'].includes(key)
				}; //populate selectedKeys

				//selectedKeys.debug("selectedKeys");

				switch (ev.abjad,

					\note, {
						[\freq, \midinote].do { |key|
							selectedKeys[key] = selectedKeys.use { ('~'++key).interpret.value } //.value strip Rest() and render fails if \dur is Rest
						}
					},

					\literal, {
						selectedKeys[\position] ?? {selectedKeys[\position] = 'after'};
					};
				);

				selectedKeys.reject {|item, key|
					if (key.isNil) {key.debug("******key.isNil*****")};
					key.isNil } ; //Is necessary?

			},
			\callback, Pfunc { |ev| ev.score.prEvents(ev.finish) }
		);
	}
}