FoscPbindScore {

	var selection, netAddress, <>instrumentPath;

	*new { |foscOutputSubdirPath, foscOutputDirectoryPath, netaddr |
		^super.new.init(foscOutputSubdirPath, foscOutputDirectoryPath, netaddr)
	}

	init { | foscOutputSubdirPath, foscOutputDirectoryPath, netaddr |
		selection = FoscSelection();
		FoscConfiguration.foscOutputDirectoryPath = foscOutputDirectoryPath;
		this.instrument(foscOutputSubdirPath);
		netAddress = netaddr;
	}

	instrument { |path|
		FoscConfiguration.foscOutputSubdirPath = path;
		^instrumentPath = path;
	}

	notes { |pbind|
		^FoscPbindPattern.new(this, pbind, \note).play;
	}

	voice { |id, pbind|
		^selection;
	}

	render { |id, pbind, format = 'svg', crop = true|
		var oscPath = FoscConfiguration.foscOutputSubdirPath;
		var route = FoscConfiguration.foscOutputSubdirPath.asString;
		var lastOutputFileName = FoscIOManager.lastOutputFileName.asString.split($.)[0]; //UNUSED
		var success, currentOutputFileName;//FoscPersistenceManager ^[outputPath, success];
//		var fileExist, doneTask, watcher;

		/* write file */
		"% format".format(format).postln;
		if (format == 'svg')
		{ success = selection.write.asSVG(crop: crop) }
		{ selection.show }; //why?

		lastOutputFileName.postln;
		currentOutputFileName = success[0].asString.split($.)[0].split($/).last;
		success.postln;
		currentOutputFileName.postln;
		//netAddress.sendMsg('newStaff', oscPath, route, currentOutputFileName);
		netAddress.sendMsg('newStaff', route, currentOutputFileName);
/*
		Entiendo que no es necesario porque write usa systemCmd (sincrónico)
			success = selection.write.asSVG(crop: crop) }


		fileExist = Task {
			while ( { File.exists(success[0]).not },
				{
					"file doesn't exists".postln;
					success[0].postln;
					File.exists(success[0]).postln;
					0.1.wait;
				}
			)
		};

		doneTask = Task {
			netAddress.sendMsg(oscPath, route, svg);
			"Render finished".postln;

		};

		watcher = SimpleController(fileExist)
		.put(\stopped, { |... args|
			watcher.remove;
			args.debug("got stopped notification");
			doneTask.play;
		});

		fileExist.play;

*/
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

		ev.instrumentPath !? { this.instrument(ev.instrumentPath) };

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