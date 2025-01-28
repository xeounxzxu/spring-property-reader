package io.xeounxzxu.property;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Order(Ordered.LOWEST_PRECEDENCE)
public class YamlPropertyEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final String SPRING_PROFILES = "spring.profiles";
	private static final String SPRING_PROFILES_ACTIVE_ON_PROFILE = "spring.config.activate.on-profile";
	private static final String FILE_LOCATION_PATTERN = "classpath:*.yml";

	private final YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String[] activeProfiles = environment.getActiveProfiles();
		var resourceLoader = new DefaultResourceLoader();
		var resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);

		try {
			Resource[] resources = resourcePatternResolver.getResources(FILE_LOCATION_PATTERN);

			for (Resource resource : resources) {
				if (!resource.exists()) {
					throw new IllegalArgumentException("Resource " + resource + " does not exist");
				}

				List<PropertySource<?>> propertySources = yamlPropertySourceLoader.load(resource.getFilename(), resource);

				propertySources.stream().filter(propertySource -> propertySource.getSource() instanceof Map)
						.forEach(propertySource -> {
							Map<String, Object> sourceMap = (Map<String, Object>) propertySource.getSource();

							Object profiles = sourceMap.getOrDefault(SPRING_PROFILES,
									sourceMap.get(SPRING_PROFILES_ACTIVE_ON_PROFILE));

							if (profiles == null) {
								addPropertySource(environment, propertySource);
							} else {
								String[] profileArray = profiles.toString().split(",");
								for (String profile : profileArray) {
									if (Stream.of(activeProfiles).anyMatch(activeProfile -> activeProfile.equals(profile.trim()))) {
										addPropertySource(environment, propertySource);
									}
								}
							}
						});
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process YAML properties", e);
		}
	}

	private void addPropertySource(ConfigurableEnvironment environment, PropertySource<?> propertySource) {
		environment.getPropertySources().addFirst(propertySource);
	}
}
