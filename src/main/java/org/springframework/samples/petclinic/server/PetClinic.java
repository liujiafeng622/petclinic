package org.springframework.samples.petclinic.server;

import org.springframework.samples.petclinic.owner.Owner;

import java.util.List;

public interface PetClinic {

	List<Owner> getOwner(Owner example);

}
