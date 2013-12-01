package agent;

import java.util.Random;
import java.util.Set;

import utils.MapKB;
import utils.VacuumMapsUtils;
import utils.VacuumMapsUtils.Movement;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;

public class AgentProgramES implements AgentProgram {

	private int step;
	public Movement lastMovement;
	private VacuumMapsUtils map;
	private Random random;

	public AgentProgramES ()
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

}
