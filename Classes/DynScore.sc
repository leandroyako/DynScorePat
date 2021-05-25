DynScore {

	var selection, netAddress, <instrumentPath, <>instrumentName;

	*new { |name, path, foscOutputDirectoryPath, netaddr |
		^super.new.init(name, path, foscOutputDirectoryPath, netaddr)
	}

	init { |name, path, foscOutputDirectoryPath, netaddr |
		selection = FoscSelection();
		FoscConfiguration.foscOutputDirectoryPath = foscOutputDirectoryPath;
		this.route(path);
		instrumentName = name;
		instrumentPath = path;
		netAddress = netaddr;
	}

	route { |path|
		FoscConfiguration.foscOutputSubdirPath = path;
	}

	name {
		^instrumentName
	}
	/*
	instrument { |path|
		FoscConfiguration.foscOutputSubdirPath = path;
		^instrumentPath = path;
	}
	*/

	notes { |pbind|
		^DynScorePat.new(this, pbind, \note).play;
	}

	newPart {
		netAddress.sendMsg('newPart', instrumentPath, this.name)
	}

	deletePart {
		netAddress.sendMsg('deletePart', instrumentPath)
	}

	scroll {
		netAddress.sendMsg('scroll', instrumentPath);
	}


	selection{
		^selection;
	}

	format {
		^FoscContainer(selection).format;
	}

	render { |id, pbind, format = 'svg', crop = true|
		var oscPath = FoscConfiguration.foscOutputSubdirPath;
		var route = FoscConfiguration.foscOutputSubdirPath.asString;
		var lastOutputFileName = FoscIOManager.lastOutputFileName.asString.split($.)[0]; //UNUSED
		var success, currentOutputFileName;//FoscPersistenceManager ^[outputPath, success];

		/* write file */
		//"% format".format(format).postln;
		if (format == 'svg')
		{ success = selection.write.asSVG(crop: crop) }
		{ selection.show }; //why?

		//lastOutputFileName.postln;
		currentOutputFileName = success[0].asString.split($.)[0].split($/).last;
		//success.postln;
		//currentOutputFileName.postln;
		netAddress.sendMsg('newStaff', route, currentOutputFileName);
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

		ev.route !? { this.route(ev.route) };

		//ev.debug("****** AFTER CLEANING NILS ******");

		if (ev.isRest)
		{
			leaf = FoscLeafMaker().([nil], [ev.dur.value]);
		} //.value strip Rest()
		{ //attach to notes only
			leaf = FoscLeafMaker().([FoscPitch(ev.freq.cpsmidi)], [ev.dur]);

		//workaround for broken FoscDynamic
		ev.dynamic !? {
			case
			{(ev.dynamic == 0) || (ev.dynamic == -inf)} { leaf[0].attach(FoscDynamic('niente', nameIsTextual:true)) } // -inf too?
			{ev.dynamic.isKindOf(Symbol) || ev.dynamic.isKindOf(String)} { leaf[0].attach(FoscDynamic(ev.dynamic, nameIsTextual:true)) }
			{ leaf[0].attach(FoscDynamic(ev.dynamic)) }
		};

		ev.articulation !? {
			if ( (ev.articulation.asString.size > 0) && (ev.articulation != ' ') )
		{ leaf[0].attach(FoscArticulation(ev.articulation)) }
		{ ev.removeAt(\articulation) }
		};
		//ev.articulation.size > 0 { leaf[0].attach(FoscArticulation(ev.articulation)) };

		ev.markup !? { leaf[0].attach(FoscMarkup(ev.markup, ev.markupDir)) };
		ev.noteheadStyle !? { leaf[0].noteHead.tweak.style = ev.noteheadStyle };
		ev.noteheadSize !? { leaf[0].noteHead.tweak.fontSize = ev.noteheadSize };
		ev.noteheadColor !? { leaf[0].noteHead.tweak.color = ev.noteheadColor };
		};

	//attach to any leaf
	ev.fermata !? { leaf[0].attach(FoscFermata(ev.fermata)) };
	//ev.markup maybe?
	selection = selection ++ leaf;
	//ev.debug("****** LAST CLEANING ******");
	}

	prModEvent { |path, msg|
		//NetAddr(host, port).sendMsg(path, msg);
	}
}