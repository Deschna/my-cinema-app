package cinema.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.config.BeanIds.SPRING_SECURITY_FILTER_CHAIN;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

class ApplicationContextHierarchyTest {
    private static final String APPLICATION_PACKAGE = "cinema.";
    private static final Set<String> ROOT_SCANNED_PACKAGES = Set.of(
            "cinema.dao.",
            "cinema.security.",
            "cinema.service."
    );
    private static final Set<String> ROOT_CLASSES_OUTSIDE_SCANNED_PACKAGES = Set.of(
            DataInitializer.class.getName(),
            RootConfig.class.getName(),
            SecurityConfig.class.getName()
    );
    private static final Set<String> SERVLET_SCANNED_PACKAGES = Set.of(
            "cinema.controller."
    );
    private static final Set<String> SERVLET_CLASSES_OUTSIDE_SCANNED_PACKAGES = Set.of(
            WebConfig.class.getName()
    );

    @Test
    void registersOnlyExpectedComponentsInRootAndServletContexts() {
        WebAppInitializer initializer = new WebAppInitializer();

        DefaultListableBeanFactory rootFactory = createBeanFactory(
                initializer.getRootConfigClasses()
        );
        Set<String> rootClassNames = beanClassNames(rootFactory);
        assertContainsOnlyExpectedApplicationComponents(
                rootClassNames,
                ROOT_SCANNED_PACKAGES,
                ROOT_CLASSES_OUTSIDE_SCANNED_PACKAGES
        );

        DefaultListableBeanFactory servletFactory = createBeanFactory(
                rootFactory,
                initializer.getServletConfigClasses()
        );
        Set<String> servletClassNames = beanClassNames(servletFactory);
        assertContainsOnlyExpectedApplicationComponents(
                servletClassNames,
                SERVLET_SCANNED_PACKAGES,
                SERVLET_CLASSES_OUTSIDE_SCANNED_PACKAGES
        );
        assertSame(rootFactory, servletFactory.getParentBeanFactory());
    }

    @Test
    void registersSecurityFilterChainOnlyInRootContextAndMakesItVisibleToServletContext() {
        WebAppInitializer initializer = new WebAppInitializer();

        Class<?>[] rootConfigClasses = initializer.getRootConfigClasses();
        Class<?>[] servletConfigClasses = initializer.getServletConfigClasses();

        DefaultListableBeanFactory rootFactory = createBeanFactory(rootConfigClasses);
        assertTrue(rootFactory.containsBeanDefinition(SPRING_SECURITY_FILTER_CHAIN));

        DefaultListableBeanFactory servletFactory = createBeanFactory(
                rootFactory,
                servletConfigClasses
        );
        assertFalse(servletFactory.containsBeanDefinition(SPRING_SECURITY_FILTER_CHAIN));
        assertTrue(servletFactory.containsBean(SPRING_SECURITY_FILTER_CHAIN));
    }

    private static DefaultListableBeanFactory createBeanFactory(
            Class<?>... configurationClasses) {
        return createBeanFactory(null, configurationClasses);
    }

    private static DefaultListableBeanFactory createBeanFactory(
            DefaultListableBeanFactory parent,
            Class<?>... configurationClasses) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory(parent);
        AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(beanFactory);
        reader.register(configurationClasses);
        ConfigurationClassPostProcessor processor = new ConfigurationClassPostProcessor();
        processor.postProcessBeanDefinitionRegistry(beanFactory);
        return beanFactory;
    }

    private static Set<String> beanClassNames(DefaultListableBeanFactory beanFactory) {
        return Arrays.stream(beanFactory.getBeanDefinitionNames())
                .map(beanFactory::getBeanDefinition)
                .map(BeanDefinition::getBeanClassName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static boolean isApplicationClass(String className) {
        return className.startsWith(APPLICATION_PACKAGE);
    }

    private static void assertContainsOnlyExpectedApplicationComponents(
            Set<String> beanClassNames,
            Set<String> scannedPackages,
            Set<String> classesOutsideScannedPackages) {
        for (String packageName : scannedPackages) {
            assertTrue(beanClassNames.stream().anyMatch(name -> name.startsWith(packageName)),
                    "No application component found in " + packageName);
        }
        Set<String> missingClasses = classesOutsideScannedPackages.stream()
                .filter(className -> !beanClassNames.contains(className))
                .collect(Collectors.toSet());
        assertTrue(missingClasses.isEmpty(),
                "Missing expected application classes: " + missingClasses);
        Set<String> unexpectedClasses = beanClassNames.stream()
                .filter(ApplicationContextHierarchyTest::isApplicationClass)
                .filter(className -> !classesOutsideScannedPackages.contains(className)
                        && scannedPackages.stream().noneMatch(className::startsWith))
                .collect(Collectors.toSet());
        assertTrue(unexpectedClasses.isEmpty(),
                "Unexpected application classes: " + unexpectedClasses);
    }
}
