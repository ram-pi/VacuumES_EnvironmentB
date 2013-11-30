package agent;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import utils.MapKB;
import utils.VacuumMapsUtils;
import utils.VacuumMapsUtils.Movement;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.AbstractAgent;
import aima.core.agent.impl.NoOpAction;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;

public class VacuumAgentES extends AbstractAgent {
	//just test
	public Action suck, left, down, right, up;



	public VacuumAgentES() {
		super(new AgentProgram() {
			private int step;
			public Movement lastMovement;
			private VacuumMapsUtils map;
			private Random random;

			/* Anonymous constructor */
			{
				this.step = 0;
				this.map = new MapKB(this);
				this.random = new Random();
			}

			@Override
			public Action execute(final Percept percept) {

				final LocalVacuumEnvironmentPerceptTaskEnvironmentB vep = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;

				if (this.step == 0) {
					this.map.setInitialTile(vep);
				}
				else {
					this.map.updateMap(vep, this.lastMovement);
				}

				this.step++;

				final Set<Action> actionsKeySet = vep.getActionEnergyCosts().keySet();


				int randomInt = this.random.nextInt(4);

				this.lastMovement = (Movement.values())[randomInt];

				System.out.println(this.lastMovement);
				System.out.println(randomInt+1);
				System.out.println(this.map);
				return (Action)actionsKeySet.toArray()[randomInt+1];


			}
		});



	}

	public void init (Set<Action> actionKeySet) {
		suck = (Action) actionKeySet.toArray()[0];
		left = (Action) actionKeySet.toArray()[1];
		down = (Action) actionKeySet.toArray()[2];
		right = (Action) actionKeySet.toArray()[3];
		up = (Action) actionKeySet.toArray()[4];
	}

}
