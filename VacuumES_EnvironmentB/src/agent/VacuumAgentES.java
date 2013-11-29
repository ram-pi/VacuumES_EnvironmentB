package agent;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

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
			@Override
			public Action execute(final Percept percept) {
				
				final LocalVacuumEnvironmentPerceptTaskEnvironmentB vep = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;
				final Set<Action> actionsKeySet = vep.getActionEnergyCosts().keySet();


				new Random();
				final int randomInt = new Random().nextInt(actionsKeySet.size());
				final Iterator<Action> iterator = actionsKeySet.iterator();
				for (int i = 0; i < randomInt; i++)
					iterator.next();
				return iterator.next();
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
