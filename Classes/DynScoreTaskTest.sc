DynScoreTaskTest {

	var <>clock, <task;

	*new { |argument|
		^super.new.init()
	}

	init { |argument|
		this.clock = TempoClock.default;
		task = Task {
			var clock = thisThread.clock;
			var currentBar = clock.bar;

			loop {
				if( clock.bar - currentBar > 0,
					{
						clock.bar.debug("bar changed");
						currentBar = clock.bar;
					}
				);
				(clock.beatDur * clock.beatsPerBar).wait;
			}
		}
	}

	autoscroll { |run = true|
		run.if
		{ task.start(this.clock)	}
		{ task.pause }
	}
}