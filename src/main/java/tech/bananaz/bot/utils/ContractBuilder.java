package tech.bananaz.bot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.bananaz.utils.DiscordUtils;
import tech.bananaz.repositories.EventPagingRepository;
import tech.bananaz.repositories.SaleConfigPagingRepository;
import tech.bananaz.bot.models.Contract;
import tech.bananaz.models.Sale;
import tech.bananaz.utils.TwitterUtils;

@Component
public class ContractBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContractBuilder.class);

	public Contract configProperties(
									Sale config,
									DiscordUtils bot,
									TwitterUtils twitBot, 
									SaleConfigPagingRepository configs, 
									EventPagingRepository events) throws RuntimeException, InterruptedException {
		Contract output = null;
		try {
			// If no server or outputChannel then throw exception
			output = new Contract();
			output.setConfigs(configs);
			output.setConfig(config);
			output.setEvents(events);
			output.setId(config.getId());
			output.setContractAddress(config.getContractAddress());
			output.setInterval(config.getInterval());
			output.setBot(bot);
			output.setTwitBot(twitBot);
			output.setExcludeDiscord(config.getExcludeDiscord());
			output.setExcludeTwitter(config.getExcludeTwitter());
			output.setShowBundles(config.getShowBundles());
			
		} catch (Exception e) {
			LOGGER.error("Check properties {}, Exception: {}", config.toString(), e.getMessage());
			throw new RuntimeException("Check properties " + config.toString() + ", Exception: " + e.getMessage());
		}
		return output;
	}

}
