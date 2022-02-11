DynScore {

	var netAddress, abjadAddress,
	foscStaff, selection, autoscrollTask, <>clock,
	<instrumentPath, <>instrumentName,
	<>initTime, <>eventType;

	*new { |name, path, outputDirectoryPath, netaddr, abjadaddr, clock|
		^super.new.init(name, path, outputDirectoryPath, netaddr, abjadaddr, clock)
	}

	init { |name, path, outputDirectoryPath, netaddr, abjadaddr|
		selection = FoscSelection();
		foscStaff = FoscStaff();

		if (
			File.exists(outputDirectoryPath),
			{
				FoscConfiguration.foscOutputDirectoryPath = outputDirectoryPath;
			},
			{
				(outputDirectoryPath + "doesn't exist").error
			}
		);

		FoscConfiguration.foscOutputSubdirPath = path;
		instrumentName = name;
		instrumentPath = path;
		netAddress = netaddr;
		abjadAddress = abjadaddr;
		clock = TempoClock.default;

		autoscrollTask = Task {
			//var clock = thisThread.clock;
			var currentBar = this.currentBar;

			loop {
				if( this.clock.bar - currentBar > 0,
					{
						clock.bar.debug("bar changed");
						//this.scroll;
						this.goto(currentBar);
						currentBar = this.currentBar;
					}
				);
				(this.clock.beatDur * this.clock.beatsPerBar).wait;
			}
		};

		OSCdef(\status,
			{ |msg|
				msg.postln;
			},
			'/dynscore/status'
		);

		OSCdef(\join,
			{ |msg|
				msg.postln;
			},
			'/dynscore/join'
		);
		OSCdef(\leave,
			{ |msg|
				msg.postln;
			},
			'/dynscore/leave'
		);
	}

	currentBar {
		^this.clock.bar
	}
	/*
	route { |path|
	FoscConfiguration.foscOutputSubdirPath = path;
	^instrumentPath = path;
	}
	*/

	name {
		^instrumentName
	}

	pattern { |pbind, overwrite = true|
		if (overwrite) { selection = FoscSelection(); foscStaff = FoscStaff() };
		DynScorePat.new(this, pbind, \note).play;
	}

	setInitTime {
		initTime ?? {
			initTime = Clock.seconds;
			abjadAddress.sendMsg("/initTime", initTime);
			initTime.debug("initTime");
		}
	}

	currentOffset {
		var offset = ((Clock.seconds - this.initTime) * 1000).asInteger;
		^offset
	}

	lastEventType {
		^eventType
	}

	fromFreqOffsetPair { |freq|
		this.setInitTime;

		freq
		!? {

			abjadAddress.sendMsg('event', this.currentOffset, freq, instrumentPath);
			eventType = \sound;
			this.currentOffset.debug("this.currentOffset");
			eventType.debug("eventType");
			freq.debug("freq");
			abjadAddress.debug("abjadAddress")
		}
		?? {
			if ( eventType != \silence,
				{
					abjadAddress.sendMsg('event', this.currentOffset, "None", instrumentPath);
					eventType = \silence;

					this.currentOffset.debug("this.currentOffset");
					eventType.debug("eventType");
					freq.debug("freq");
				},
				{
					("Silence event ignored: last event was " ++ eventType).postln;
				}
			)
		}
	}

	newPart {
		netAddress.sendMsg('newPart', instrumentPath, this.name)
	}

	deletePart {
		netAddress.sendMsg('deletePart', instrumentPath)
	}

	scroll { |nextBar = true, beats = 4|
		var scheduled = false;
		nextBar.if
		{
			scheduled.if
			{
				"scroll already scheduled on next bar".postln;
				//scheduled.debug("scheduled.debug")

			}
			{
				scheduled = true;
				//scheduled.debug("scheduled.debug");
				//this.clock.playNextBar(
				netAddress.sendMsg('scrollNextBeats', instrumentPath, beats);
				this.clock.sched(beats,
					{
						netAddress.sendMsg('scroll', instrumentPath);
						scheduled = false;
						//scheduled.debug("scheduled.debug");
					}
				)
			}
		}
		{
			netAddress.sendMsg('scroll', instrumentPath)
		}
	}

	goto { |bar|
		netAddress.sendMsg('goto', instrumentPath, bar);
	}

	autoscroll { |run = true|
		run.if
		{ autoscrollTask.start(this.clock) }
		{ autoscrollTask.pause }
	}

	staff{
		^foscStaff.add(selection);
	}

	format {
		^this.staff.format;
	}

	render { |id, pbind, format = 'svg', crop = true|
		var currentOutputFileName, tmpFolder, renderFolder, previewSubdir, renderSubdir, lastRender, lastPreview, lastRenderNum, lastPreviewNum, confirmPreview;

		id.debug("id");
		pbind.debug("pbind");

		previewSubdir = this.instrumentPath ++ "/tmp_prev/";
		renderSubdir = this.instrumentPath ++ "/";
		tmpFolder = PathName.new(FoscConfiguration.foscOutputDirectoryPath ++ previewSubdir);
		renderFolder = PathName.new(FoscConfiguration.foscOutputDirectoryPath ++ renderSubdir);


		renderFolder.files.last !? {
			lastRenderNum = renderFolder.files.last.fileNameWithoutExtension.asInteger;
			lastRender = renderFolder.files.last.fileName;
		};
		tmpFolder.files.last !? {lastPreviewNum = tmpFolder.files.last.fileNameWithoutExtension.asInteger;
			lastPreview = tmpFolder.files.last.fileName;
		};

		confirmPreview = try {

			lastRenderNum ?? {lastRenderNum = 0};

			lastPreviewNum.debug("lastPreviewNum");
			lastRenderNum.debug("lastRenderNum");

			lastPreviewNum == (lastRenderNum + 1);


		} {false};

		confirmPreview.debug("confirmPreview");

		if (confirmPreview,
			{
				var croppedFileName = lastPreview.split($.).insert(1, "cropped").join(".");
				var outputPath = tmpFolder.pathOnly ++ lastPreview;
				var outputCroppedPath = tmpFolder.pathOnly ++ croppedFileName;

				"make preview definitve".postln;
				/*
				outputCroppedPath.debug("outputCroppedPath");
				outputPath.debug("outputPath");
				lastPreview.debug("lastPreview");
				tmpFolder.pathOnly.debug("tmpFolder");
				previewSubdir.debug("previewSubdir");
				*/

				//make preview definitve
				//move file to render folder and update

				[outputPath, outputCroppedPath].do { |file|
					var path = PathName(file);
					//var deleted;
					File.copy(file, renderFolder.pathOnly ++ path.fileName );
					File.delete(file);
					//deleted = File.delete(file);
					//deleted.debug("deleted")
				};
				currentOutputFileName = PathName(outputPath).fileNameWithoutExtension;
				currentOutputFileName.debug("currentOutputFileName");
				netAddress.sendMsg('newStaff', this.instrumentPath, currentOutputFileName);
			},
			{
				"render new file and update".postln;
				currentOutputFileName = this.prSvg(id, pbind, format, crop, renderSubdir);
				//currentOutputFileName.debug("currentOutputFileName render");
				netAddress.sendMsg('newStaff', this.instrumentPath, currentOutputFileName);

		})
	}

	preview { |id, pbind, format = 'svg', crop = true, clear = false|

		if (clear.not, {
			var currentOutputFileName = this.prSvg(id, pbind, format, crop, this.instrumentPath);

			var previewSubdir = this.instrumentPath ++ "/tmp_prev/";

			var tmpFolder = PathName.new(FoscConfiguration.foscOutputDirectoryPath ++ previewSubdir);
			var renderFolder = PathName.new(FoscConfiguration.foscOutputDirectoryPath ++ this.instrumentPath ++ "/");
			var croppedFileName = currentOutputFileName ++ ".cropped.svg";
			var outputPath = renderFolder.pathOnly ++ currentOutputFileName ++ ".svg";
			var outputCroppedPath = renderFolder.pathOnly ++ croppedFileName;

			File.mkdir(tmpFolder.fullPath);

			[outputPath, outputCroppedPath].do { |file|
				var path = PathName(file);
				var newPath = path.pathOnly ++ "tmp_prev/" ++ path.fileName;
				File.delete(newPath); //delete old file and overwrite
				File.copy(file, newPath);
				File.delete(file);
			};

			/*
			outputPath.debug("outputPath preview");
			outputCroppedPath.debug("outputCroppedPath preview");
			*/

			netAddress.sendMsg('previewStaff', this.instrumentPath, currentOutputFileName);
		},
		{
			var previewSubdir = this.instrumentPath ++ "/tmp_prev/";
			var tmpFolder = PathName.new(FoscConfiguration.foscOutputDirectoryPath ++ previewSubdir);

			File.deleteAll(tmpFolder.fullPath);

			netAddress.sendMsg('clearPreview', this.instrumentPath);
		})
	}

	prSvg { |id, pbind, format = 'svg', crop = true, subdirPath|
		var success, currentOutputFileName; //from FoscPersistenceManager ^[outputPath, success];
		FoscConfiguration.foscOutputSubdirPath = subdirPath;

		/* write file */
		if (format == 'svg')
		{ success = this.staff.write.asSVG(crop: crop) }
		{ "DynScoreViz needs svg format to render properly".warn; this.staff.show }; //maybe png could be a worse but valid alternative
		^currentOutputFileName = success[0].asString.split($.)[0].split($/).last;
	}

	prEvents { |ev|
		var leaf;

		ev.keysValuesDo { |key, value|
			if (value === Nil)
			{ ev.removeAt(key) }
			{ ev.put(key, value) }
		};

		ev.route !? { this.route(ev.route) };

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
	}

	prModEvent { |path, msg|
		"not implemented yet".error;
		//NetAddr(host, port).sendMsg(path, msg);
	}
}