FoscPbindScore {

	classvar selection;

	init {
		selection = FoscSelection();
	}

	notes { |pbind|
		^FoscPbindPattern.new(this, pbind, \note).play;
	}

	voice { |id, pbind|
		^selection;
	}

	render { |id, pbind|

		selection.show;
		//selection.a.write.asSVG(crop: true);

		/*

		if len(music) > 1:
		  music.simultaneous = True
		  for i, voice in enumerate(music):
		    if i % 2 == 0: #if voice number is even
		      direction = Down
		    else:
		      direction = Up
		 voice_direction[voice.name] = direction
		 override(voice).stem.direction = direction
		    else:
		      voice = music[0]
		      voice_direction[voice.name] = None

		output_path = args.instrument+'/svg/output'
		*/
	}

	preview { |id, pbind|
		"not implemented yet".error;

		/*
		if preview == True:
		output_path = './preview/'+output_path
		colors = ['blue', 'darkblue', 'cyan', 'darkcyan']
		for voice_num, voice in enumerate(music):
		for leaf_num, leaf in enumerate(voice):
		wrapper = inspect(leaf).wrappers(Markup)
		try:
		tag = wrapper[0].tag
		if tag == Tag('PREVIEW'):
		detach(Markup, leaf)
		except:
		None

		number = str(voice_num) + '-' + str(leaf_num)
		markup = Markup(number, direction = voice_direction[voice.name]).tiny().with_color(colors[voice_num])
		#attach(markup, leaf, tag='PREVIEW')
		voice_markup = Markup('Voice '+ str(voice_num) + ':' + voice.name, direction = voice_direction[voice.name]).box().with_color(colors[voice_num])
		#attach(voice_markup, voice[0], tag='PREVIEW')
		id_markup = Markup('ID: ' + id, direction = Up).box().with_color(SchemeColor('purple'))
		#attach(id_markup, select(music).leaves()[0], tag='PREVIEW')
		*/
	}

	prEvents { |ev|
		var leaf;

		//ev.debug("****** BEFORE CLEANING NILS ******");

		ev.keysValuesDo { |key, value|
			if (value === Nil)
			{ ev.removeAt(key) }
			{ ev.put(key, value) }
		};

		//ev.debug("****** AFTER CLEANING NILS ******");
		//("").postln;

		if (ev.isRest)
		{ leaf = FoscLeafMaker().([nil], [ev.dur.value]) } //.value strip Rest()
		{ leaf = FoscLeafMaker().([FoscPitch(ev.freq.cpsmidi)], [ev.dur]) };

		//workaround for broken FoscDynamic
		ev.dynamic !? {
			case
			{(ev.dynamic == 0) || (ev.dynamic == -inf)} { leaf[0].attach(FoscDynamic('niente', nameIsTextual:true)) } // -inf too?
			{ev.dynamic.isKindOf(Symbol) || ev.dynamic.isKindOf(String)} { leaf[0].attach(FoscDynamic(ev.dynamic, nameIsTextual:true)) }
			{ leaf[0].attach(FoscDynamic(ev.dynamic)) }
		};

		ev.articulation !? { leaf[0].attach(FoscArticulation(ev.articulation)) };
		ev.fermata !? { leaf[0].attach(FoscFermata(ev.fermata)) };
		ev.markup !? { leaf[0].attach(FoscMarkup(ev.markup, ev.markupDir)) };
		ev.noteheadStyle !? { leaf[0].noteHead.tweak.style = ev.noteheadStyle };
		ev.noteheadSize !? { leaf[0].noteHead.tweak.fontSize = ev.noteheadSize };
		ev.noteheadColor !? { leaf[0].noteHead.tweak.color = ev.noteheadColor };

		selection = selection ++ leaf;

	}

	prModEvent { |path, msg|
		//NetAddr(host, port).sendMsg(path, msg);
		//mmm no estoy seguro si se usa
	}
}