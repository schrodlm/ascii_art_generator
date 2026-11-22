package clascii.cli.parser

import clascii.cli.config.CliConfig
/**
 * A trait defining a parser for ASCII art configuration.
 *
 * This trait represents a contract for implementing parsers that can read and interpret
 * configuration data for generating ASCII art. Implementations of this trait should provide
 * the logic to parse configuration data from a specific source or format.
 */
trait Parser {

  /**
   * Parses the configuration data and constructs a `CliConfig` object.
   *
   * This method is responsible for reading and interpreting the configuration data necessary
   * to generate ASCII art. It should handle the parsing logic specific to the data source or format
   * and construct a `CliConfig` object based on the parsed data.
   *
   * @return An `Option[CliConfig]` which is `Some(CliConfig)` if parsing is successful,
   *         or `None` if parsing fails or the data is invalid. This approach allows for handling
   *         parsing errors or missing data gracefully.
   */
  def parse() : Option[CliConfig]
}
