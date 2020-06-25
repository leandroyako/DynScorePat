FoscPbindScore {

	classvar selection;

	init {
		selection = FoscSelection();
	}

	notes { |id, pbind|
		^FoscPbindPattern.new(this, id.asSymbol, pbind, \note).play;
	}

	render { |id, pbind|

		selection.show;
		//selection.a.write.asSVG(crop: true);

		/*
		music = LeafGenerator.container[self.id]
		voice_direction = {}
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

		lilypond_file = LilyPondFile.new(
		music = music,
		includes = self.includes
		)
		make_ly = persist(lilypond_file).as_ly()
		ly_path = make_ly[0]
		cmd = ['lilypond',
		'-dcrop',
		'-dno-point-and-click',
		'-ddelete-intermediate-files',
		'-dbackend=svg',
		'-o' + output_path,
		ly_path]
		subprocess.run(cmd)
		*/
		//"/display" preview: False
	}

	preview { |id, pbind|
		"not implemented yet".postln;
		//"/display" preview: True
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
		//leaf generator
		var leaf;

		ev.isRest.debug("isRest");

		if (ev.isRest)
		{ leaf = FoscLeafMaker().([nil], [ev.dur.value]) }
		{ leaf = FoscLeafMaker().([FoscPitch(ev.freq.cpsmidi)], [ev.dur]) }; //.value strip Rest()

		ev.dynamic !? { leaf[0].attach(FoscDynamic(ev.dynamic)) };
		ev.articulation !? { leaf[0].attach(FoscArticulation(ev.articulation)) };
		ev.fermata !? { leaf[0].attach(FoscFermata(ev.fermata)) };
		ev.markup !? { leaf[0].attach(FoscMarkup(ev.markup)) };
		ev.notehead !? { leaf[0].noteHead.tweak.style = ev.notehead };

		selection = selection ++ leaf;

	}

	prModEvent { |path, msg|
		//NetAddr(host, port).sendMsg(path, msg);
		//mmm no estoy seguro si se usa
	}
}