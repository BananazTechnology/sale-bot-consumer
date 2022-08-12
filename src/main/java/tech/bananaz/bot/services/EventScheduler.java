package tech.bananaz.bot.services;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;
import tech.bananaz.bot.models.Contract;
import tech.bananaz.enums.EventType;
import tech.bananaz.models.Event;
import static java.util.Objects.nonNull;
import tech.bananaz.repositories.EventPagingRepository;

public class EventScheduler extends TimerTask {
	
	private Contract contract;
	private EventPagingRepository repo;
	@Getter
	private boolean active						   = false;
	private Timer timer 		 				   = new Timer(); // creating timer
    private TimerTask task; // creating timer task
	private static final Logger LOGGER 			   = LoggerFactory.getLogger(EventScheduler.class);

	public EventScheduler(Contract contract) {
		this.contract = contract;
		this.repo     = contract.getEvents();
	}
	
	@Override
	public void run() {
		if(nonNull(this.contract) && this.active && this.contract.isActive()) {
			try {
				watchSales();
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error(String.format("Failed during get events: %s, stack: %s", this.contract.getContractAddress(), Arrays.toString(e.getStackTrace()))); 
			}
		}
	}

	public boolean start() {
		// Creates a new integer between 1-5 and * by 1000 turns it into a second in milliseconds
		// first random number
		int startsIn = (ThreadLocalRandom.current().nextInt(1, 10)*1000);
		if(nonNull(this.contract)) {
			this.active = true;
			this.task   = this;
			LOGGER.info(String.format("Starting new EventScheduler in %sms for: %s", startsIn, this.contract.toString()));
			// Starts this new timer, starts at random time and runs per <interval> milliseconds
			this.timer.schedule(task, startsIn , this.contract.getInterval());
		}
		return this.active;
	}
	
	public boolean stop() {
		this.active = false;
		LOGGER.info("Stopping SaleScheduler on " + this.contract.toString());
		return this.active;
	}
	
	private void watchSales() throws Exception {
		// Get any new items
		List<Event> queryEvents = 
			repo.findByConfigIdAndConsumedFalseAndEventTypeOrderByCreatedDateAsc(this.contract.getId(), EventType.SALE);
		// Process if events exist
		if(queryEvents.size() > 0) {
			// Loop through available items
			for(Event e : queryEvents) {
				// Ensure a single transaction of GET and SET which should ensure no overwrite
				int updateCount = repo.updateByIdSetConsumedTrueAndConsumedBy(e.getId(), this.contract.getUuid());
				// Ensure the item was updated
				if(nonNull(updateCount) && updateCount > 0) {
					Event refreshedEvent = repo.findById(e.getId());
					// Ensure the item is consumed and the owner is this contract instance
					if(refreshedEvent.isConsumed() && refreshedEvent.getConsumedBy().equalsIgnoreCase(this.contract.getUuid())) {
						// Log
						logInfoNewEvent(e);
						// Discord
						try {
							 if(!this.contract.isExcludeDiscord()) this.contract.getBot().sendEvent(e, this.contract.getConfig());
						} catch (Exception ex) {
							LOGGER.error("Error on Discord dispatch of contract id {} with excpetion {} - {}", this.contract.getId(), ex.getCause(), ex.getMessage());
						}
						// Twitter
						try {
							if(!this.contract.isExcludeTwitter()) this.contract.getTwitBot().sendEvent(e, this.contract.getConfig());
						} catch (Exception ex) {
							LOGGER.error("Error on Twitter dispatch of contract id {} with excpetion {} - {}", this.contract.getId(), ex.getCause(), ex.getMessage());
						}
					}
				}
			}
		}
	}
	
	private void logInfoNewEvent(Event event) {
		LOGGER.info("{}, {}", event.toString(),this.contract.toString());
	}
}
