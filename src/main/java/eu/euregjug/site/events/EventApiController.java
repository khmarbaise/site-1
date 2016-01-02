/*
 * Copyright 2015 EuregJUG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.euregjug.site.events;

import eu.euregjug.site.posts.PostEntity;
import eu.euregjug.site.posts.PostRepository;
import eu.euregjug.site.support.ResourceNotFoundException;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * @author Michael J. Simons, 2015-12-27
 */
@RestController
@RequestMapping("/api/events")
class EventApiController {

    private final EventRepository eventRepository;
    
    private final PostRepository postRepository;

    @Autowired
    public EventApiController(EventRepository eventRepository, PostRepository postRepository) {
	this.eventRepository = eventRepository;
	this.postRepository = postRepository;
    }

    @RequestMapping(method = POST)
    @PreAuthorize("isAuthenticated()")
    public EventEntity create(final @Valid @RequestBody EventEntity newEvent) {	
	return this.eventRepository.save(newEvent);
    }
    
    // TODO: Choose wether to create a completly new posts or not. For the time being: just select on
    @RequestMapping(value = "/{id:\\d+}/post/{postId:\\d+}", method = PUT)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public EventEntity addPost(final @PathVariable Integer id, final @PathVariable Integer postId) {
	final EventEntity eventEntity = this.eventRepository.findOne(id);
	final PostEntity postEntity =  this.postRepository.findOne(postId);
	if(eventEntity == null || postEntity == null) {
	    throw new ResourceNotFoundException();
	}
	eventEntity.setPost(postEntity);
	return eventEntity;
    }

    @RequestMapping(method = GET)
    public Page<EventEntity> get(final Pageable pageable) {
	return this.eventRepository.findAll(pageable);
    }
}
