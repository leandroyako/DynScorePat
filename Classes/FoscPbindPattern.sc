FoscPbindPattern {
	var <score, <id, <pbind, <type;

	classvar patternBuilder;

	*new { |score, id, pbind, type=\note|
		^super.newCopyArgs(score, id, pbind, type);
	}

	*initClass {
		patternBuilder = this.prPatternBuilder;
	}

	play {
		(patternBuilder <> (abjad: type, id: id, score: score) <> pbind).play;
	}

	render {
		score.render(this.id);
	}

	preview {
		score.preview(this.id);
	}

	*prPatternBuilder {
		^Pbind(
			\delta, 0,
			\amp, 0,
			\finish, {|ev|
				var selectedKeys;

				selectedKeys = ev.reject({
					|item, key|
					['delta', 'path', 'score', 'abjad', 'finish'].includes(key)
					}
				); //populate selectedKeys

				switch ( ev.abjad,

					\note, {
							[\freq, \midinote].do({|key|
							selectedKeys[key] = selectedKeys.use({ ('~'++key).interpret.value }); //.value strip Rest() and render fails if \dur is Rest
						});
/*
						selectedKeys = selectedKeys.reject( //note event cleaning. Necessary?
							{
								|item, key|
								['scale', 'root', 'ctranspose', 'mtranspose'].includes(key)
							}
						);
*/					},

					\literal, {	selectedKeys[\position] ?? {selectedKeys[\position] = 'after'}; //set default position
/*						selectedKeys = selectedKeys.reject( //literal event cleaning. Necessary?
							{ |item, key|
								['dur', 'freq', 'amp', 'rest','root','ctranspose', 'mtranspose'].includes(key)
							}
						);
					*/}
				);

				selectedKeys.reject( {|item, key| key.isNil } );

			},
			\callback, Pfunc {|ev| ev.score.prEvents(ev.finish);}
		);
	}
}