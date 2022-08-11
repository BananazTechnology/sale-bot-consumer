package tech.bananaz.bot.models;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import lombok.ToString.Exclude;
import tech.bananaz.utils.DiscordUtils;
import tech.bananaz.repositories.EventPagingRepository;
import tech.bananaz.repositories.SaleConfigPagingRepository;
import tech.bananaz.bot.services.EventScheduler;
import tech.bananaz.models.Sale;
import tech.bananaz.utils.TwitterUtils;

@ToString(includeFieldNames=true)
@Data
public class Contract {
	
	@Exclude
	@JsonIgnore
	private EventScheduler newRequest;
	
	@Exclude
	@JsonIgnore
	private SaleConfigPagingRepository configs;
	
	@Exclude
	@JsonIgnore
	private EventPagingRepository events;

	// Pairs from DB definition
	private long id;
	private String contractAddress;
	private int interval;
	private boolean active 			  = true;

	// OpenSea settings
	// Supports burning
	private boolean burnWatcher 	  = false;
	// Supports minting
	private boolean mintWatcher 	  = false;
	// For bundles support
	private boolean showBundles 	  = true;

	// Discord Settings
	@Exclude
	@JsonIgnore
	private DiscordUtils bot;
	boolean excludeDiscord = false;

	// Twitter Settings
	@Exclude
	@JsonIgnore
	private TwitterUtils twitBot;
	private boolean excludeTwitter 	  = false;
	
	// For the DB and API
	private String uuid				  = UUID.randomUUID().toString();
	
	// To save on DB calls
	@Exclude
	@JsonIgnore
	Sale config;

	public void startSalesScheduler() {
		newRequest = new EventScheduler(this);
		newRequest.start();
	}
	
	public void stopSalesScheduler() {
		newRequest.stop();
	}
	
	public boolean getIsSchedulerActive() {
		return this.newRequest.isActive();
	}
}
