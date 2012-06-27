package de.dailab.aot.sose2011.blackboard;

import java.util.Set;

import javax.security.auth.DestroyFailedException;

import org.sercho.masp.space.SimpleObjectSpace;
import org.sercho.masp.space.event.EventedSpaceWrapper;
import org.sercho.masp.space.event.EventedTupleSpace;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.EventedSpaceWrapper.SpaceDestroyer;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.knowledge.IFact;

public class BlackboardNodeBean extends AbstractAgentNodeBean implements Blackboard {
	
	private SpaceDestroyer<IFact> destroyer = null;
	private EventedTupleSpace<IFact> space = null;

	@Override
	public void doInit() {
		destroyer = EventedSpaceWrapper.getSpaceWithDestroyer(new SimpleObjectSpace<IFact>("FactBase"));
		space = destroyer.destroybleSpace;
	}

	@Override
	public void doCleanup() {
		try {
			destroyer.destroy();
		}
		catch (DestroyFailedException e) {
			e.printStackTrace();
		}
		space = null;
		destroyer = null;
	}

	public <E extends IFact> E read(E template) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		return space.read(template);
	}
	
	public <E extends IFact> Set<E> readAll(E template) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		return space.readAll(template);
	}

	public <E extends IFact> Set<E> readAllOfType(Class<E> c) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		return space.readAllOfType(c);
	}

	public <E extends IFact> E remove(E template) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		return space.remove(template);
	}

	public <E extends IFact> E remove(E template, long timeout) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		return space.remove(template, timeout);
	}

	public <E extends IFact> Set<E> removeAll(E template) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		return space.removeAll(template);
	}

	public <E extends IFact> boolean update(E template, E pattern) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		return space.update(template, pattern);
	}

	public void write(IFact fact) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		space.write(fact);
	}

	public void attach(SpaceObserver<? super IFact> observer) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		space.attach(observer);
	}

	public void attach(SpaceObserver<? super IFact> observer, IFact template) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		space.attach(observer, template);
	}

	public void detach(SpaceObserver<? super IFact> observer) {
		if (space == null) {
			throw new RuntimeException("Blackboard has not yet been initialized!");
		}
		space.detach(observer);
	}
}
