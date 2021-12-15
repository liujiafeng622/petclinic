package org.springframework.samples.petclinic.server;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class PetClinicImpl implements PetClinic {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private OwnerRepository ownerRepository;

	@Override
	public List<Owner> getOwner(Owner example) {
		if (example.getId() != null) {
			Owner byId = ownerRepository.findById(example.getId());
			if (byId != null) {
				return Arrays.asList(byId);
			}
		}
		else if (StringUtils.isNotBlank(example.getLastName())) {
			Collection<Owner> byLastName = ownerRepository.findByLastName(example.getLastName());
			if (byLastName.size() > 0) {
				return Lists.newArrayList(byLastName);
			}
		}
		else {
			return ownerRepository.findAll();
		}
		return Collections.emptyList();
	}

}
