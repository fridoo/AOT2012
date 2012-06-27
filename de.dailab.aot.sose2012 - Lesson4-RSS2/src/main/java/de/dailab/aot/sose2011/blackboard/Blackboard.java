package de.dailab.aot.sose2011.blackboard;

import java.util.Set;

import org.sercho.masp.space.event.SpaceObserver;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public interface Blackboard {

	<E extends IFact> E read(E template);

	<E extends IFact> Set<E> readAll(E template);

	<E extends IFact> Set<E> readAllOfType(Class<E> c);

	<E extends IFact> E remove(E template);

	<E extends IFact> E remove(E template, long timeout);

	<E extends IFact> Set<E> removeAll(E template);

	<E extends IFact> boolean update(E template, E pattern);

	void write(IFact fact);

	void attach(SpaceObserver<? super IFact> observer);

	void attach(SpaceObserver<? super IFact> observer, IFact template);

	void detach(SpaceObserver<? super IFact> observer);

	String getBeanName();

}
