package io.xeounxzxu.property;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Order(Ordered.LOWEST_PRECEDENCE)
public class PropertyEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String[] activeProfiles = environment.getActiveProfiles();
		ResourceLoader resourceLoader = Optional.ofNullable(application.getResourceLoader())
				.orElse(new DefaultResourceLoader());
		PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver(
				resourceLoader);
		try {

			Resource[] resources = pathMatchingResourcePatternResolver.getResources(PAHT);
			List<Resource> list = Arrays.stream(resources).toList();
			list.forEach(resource -> {

				if (!resource.exists()) {
					throw new RuntimeException("Could not find resource: " + resource);
				}

			});

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String PAHT = "classpath*:/*.yml";
}
