package tech.bananaz.bot.services;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.bananaz.bot.models.Contract;
import tech.bananaz.bot.models.ContractCollection;
import tech.bananaz.bot.utils.ContractBuilder;
import tech.bananaz.models.DiscordConfig;
import tech.bananaz.models.Sale;
import tech.bananaz.models.TwitterConfig;
import tech.bananaz.repositories.EventPagingRepository;
import tech.bananaz.repositories.SaleConfigPagingRepository;
import tech.bananaz.utils.DiscordUtils;
import tech.bananaz.utils.TwitterUtils;

import static java.util.Objects.nonNull;
import java.awt.Color;

import static tech.bananaz.utils.EncryptionUtils.decryptSale;
import static tech.bananaz.utils.StringUtils.nonEquals;

@Component
public class UpdateScheduler extends TimerTask {
	
	// Security
	@Value("${bot.encryptionKey}")
	private String key;
	
	@Autowired
	private SaleConfigPagingRepository configs;
	
	@Autowired
	private ContractCollection contracts;
	
	@Autowired
	private EventPagingRepository events;
	
	/** Important variables needed for Runtime */
	private final int REFRESH_REQ = 60000;
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateScheduler.class);
	private Timer timer = new Timer(); // creating timer
    private TimerTask task; // creating timer task
    private boolean active = false;
	
	public boolean start() {
		if(nonNull(this.contracts)) {
			this.active = true;
			this.task   = this;
			LOGGER.info(String.format("Starting new UpdateScheduler"));
			// Starts this new timer, starts at random time and runs per <interval> milliseconds
			this.timer.schedule(task, 1, REFRESH_REQ);
		}
		return active;
	}
	
	public boolean stop() {
		this.active = false;
		LOGGER.info("Stopping UpdateScheduler");
		return active;
	}

	@Override
	public void run() {
		if(nonNull(this.contracts) && active) {
			try {
				Iterable<Sale> allSaleConfigs = this.configs.findAll();
				for(Sale conf : allSaleConfigs) {
					try {
						// Must use decrypted values
						Sale decryptedConf = decryptSale(this.key, conf);
						
						List<String> updatedItems = new ArrayList<>();
						Contract cont = this.contracts.getContractById(decryptedConf.getId());
						// Update existing object in memory
						if(nonNull(cont)) {
							// Strings and Integers
							// Contract Address
							if(nonEquals(cont.getContractAddress(), decryptedConf.getContractAddress())) {
								updatedItems.add(String.format("contractAddress: %s->%s", cont.getContractAddress(), decryptedConf.getContractAddress()));
								cont.setContractAddress(decryptedConf.getContractAddress());
							}
							// Interval
							if(nonEquals(cont.getInterval(), decryptedConf.getInterval())) {
								updatedItems.add(String.format("interval: %s->%s", cont.getInterval(), decryptedConf.getInterval()));
								cont.setInterval(decryptedConf.getInterval());
							}
	
							// Booleans
							// Show Bundles
							if(nonEquals(cont.isShowBundles(), decryptedConf.getShowBundles())) {
								updatedItems.add(String.format("showBundles: %s->%s", cont.isShowBundles(), decryptedConf.getShowBundles()));
								cont.setShowBundles(decryptedConf.getShowBundles());
							}
							// Exclude Discord
							if(nonEquals(cont.isExcludeDiscord(), decryptedConf.getExcludeDiscord())) {
								updatedItems.add(String.format("excludeDiscord: %s->%s", cont.isExcludeDiscord(), decryptedConf.getExcludeDiscord()));
								cont.setExcludeDiscord(decryptedConf.getExcludeDiscord());
							}
							// Exclude Twitter
							if(nonEquals(cont.isExcludeTwitter(), decryptedConf.getExcludeTwitter())) {
								updatedItems.add(String.format("excludeDiscord: %s->%s", cont.isExcludeTwitter(), decryptedConf.getExcludeTwitter()));
								cont.setExcludeTwitter(decryptedConf.getExcludeTwitter());
							}
							// Active
							if(nonEquals(cont.isActive(), decryptedConf.getActive())) {
								updatedItems.add(String.format("active: %s->%s", cont.isActive(), decryptedConf.getActive()));
								cont.setActive(decryptedConf.getActive());
							}
							
							// Discord
							if(nonNull(cont.getBot())) {
								if(!cont.getBot().isTokenEqual(decryptedConf.getDiscordToken()) && nonNull(decryptedConf.getDiscordToken())) {
									updatedItems.add(String.format("discordToken"));
									cont.setBot(new DiscordConfig().configProperties(decryptedConf));
								}
								// Only write these values when we know a Discord has been created
								if(nonNull(cont.getBot().getBot())) {
									if(!cont.getBot().isChannelIdEqual(decryptedConf.getDiscordChannelId())) {
										updatedItems.add(String.format("discordChannelId: %s", decryptedConf.getDiscordChannelId()));
										cont.getBot().setServerTextChannel(decryptedConf.getDiscordChannelId());
									}
									
									Color color = (nonNull(decryptedConf.getDiscordMessageColor())) ? new Color(decryptedConf.getDiscordMessageColor()) : Color.ORANGE;
									if(!cont.getBot().isColorRgbEqual(color)) {
										updatedItems.add(String.format("discordMessageColor: %s", decryptedConf.getDiscordMessageColor()));
										cont.getBot().setColor(color);
									}
								}
							}
	
							// Twitter
							if(nonNull(cont.getTwitBot())) {
								if(!cont.getTwitBot().apiKeyEquals(decryptedConf.getTwitterApiKey()) || !cont.getTwitBot().apiKeySecretEquals(decryptedConf.getTwitterApiKeySecret())) {
									updatedItems.add(String.format("twitterBot"));
									cont.setTwitBot(new TwitterConfig().configProperties(decryptedConf));
								}
							}
	
						} 
						// Add new contract
						else {
							LOGGER.debug("Object NOT found in memory, building new");
							try {
								// Build required components for each entry
								TwitterUtils twitBot = new TwitterConfig().configProperties(decryptedConf);
								DiscordUtils bot = new DiscordConfig().configProperties(decryptedConf);
								Contract watcher = new ContractBuilder().configProperties(decryptedConf, bot, twitBot, this.configs, this.events);
								// Start the watcher
								watcher.startSalesScheduler();
								// Add this to internal memory buffer
								this.contracts.addContract(watcher);
								updatedItems.add(String.format("new: %s", watcher));
							} catch (Exception e) {
								LOGGER.error("Failed starting config with id {}, exception {}", conf.getId(), e.getMessage());
							}
						}
						if(updatedItems.size() > 0) {
							if(nonNull(cont)) cont.setConfig(conf);
							LOGGER.info("Contract {} updated {}", conf.getId(), Arrays.toString(updatedItems.toArray()));
						}
					} catch(Exception ex) {
						LOGGER.error("Failed inital parsing on id {}, exception {}", conf.getId(), ex.getMessage());
					}
				}
			} catch (Exception e) {}
		}
		

		// Cleanup
		/// Remove contracts which are marked not active
		for(Contract c : this.contracts.getContracts()) {
			if(!c.isActive()) {
				LOGGER.info("Object was not active, removing: {}", c.toString());
				removeContract(c);
			}
		}
		/// Ensure all contratcs in memory exist in the DB
		for(Contract cont : contracts.getContracts()) {
			try {
				boolean existsInDB = this.configs.existsById(cont.getId());
				if(!existsInDB) {
					LOGGER.info("Object was not in the db, removing: {}", cont.toString());
					removeContract(cont);
					// A limitation of this modification
					// An active loop cannot be modified then continued to loop
					// Any extra contracts removed next time
					break;
				}
			} catch (Exception e) {}
		}
	}
	
	private void removeContract(Contract c) {
		c.stopSalesScheduler();
		this.contracts.removeContract(c);
	}
}