DynScoreRoutineTest {

	var <>clock, autoscrollRoutine, autoscrollPlayer;

	*new { |clock|
		^super.new.init(clock)
	}

	init { |clock|
		clock ?? {clock = TempoClock.default };
	}

	autoscroll { |run = true|
		run.if
		{
			autoscrollRoutine = Routine{
			var currentBar = this.clock.bar;
			loop{
				if( this.clock.bar - currentBar > 0,
					{
						this.clock.bar.debug("bar changed");
						this.clock.beatDur.yieldAndReset;
					}
				)
			}
		};

		autoscrollPlayer = autoscrollRoutine.reset.play

		}
		{ autoscrollPlayer.stop }
	}
}