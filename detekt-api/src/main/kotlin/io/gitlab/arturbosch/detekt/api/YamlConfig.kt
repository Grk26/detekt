package io.gitlab.arturbosch.detekt.api

import org.yaml.snakeyaml.Yaml
import java.io.BufferedReader
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

/**
 * Config implementation using the yaml format. SubConfigurations can return sub maps according to the
 * yaml specification.
 *
 * @author Artur Bosch
 */
@Suppress("UNCHECKED_CAST")
class YamlConfig internal constructor(val properties: Map<String, Any>) : BaseConfig() {

	override fun subConfig(key: String): Config {
		val subProperties = properties.getOrElse(key) { mapOf<String, Any>() }
		return YamlConfig(subProperties as Map<String, Any>)
	}

	override fun <T : Any> valueOrDefault(key: String, default: T): T {
		val result = properties[key]
		return valueOrDefaultInternal(result, default) as T
	}

	override fun toString(): String {
		return "YamlConfig(properties=$properties)"
	}

	companion object {

		val YAML = ".yml"

		/**
		 * Factory method to load a yaml configuration. Given path must exist and end with "yml".
		 */
		fun load(path: Path): Config {
			require(Files.exists(path) && path.toString().endsWith(YAML)) { "File does not exist or end with .yaml!" }
			return load(Files.newBufferedReader(path))
		}

		/**
		 * Factory method to load a yaml configuration from a URL.
		 */
		fun loadResource(url: URL): Config {
			val reader = url.openStream().bufferedReader()
			return load(reader)
		}

		private fun load(reader: BufferedReader): Config = reader.use {
			val yamlInput = it.readText()
			if (yamlInput.isEmpty()) {
				Config.empty
			} else {
				val map = Yaml().load(yamlInput)
				if (map is Map<*, *>) {
					YamlConfig(map as Map<String, Any>)
				} else {
					throw Config.InvalidConfigurationError()
				}
			}
		}

		private fun BufferedReader.readText() = lineSequence().joinToString("\n")

	}
}
