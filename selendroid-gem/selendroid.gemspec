# -*- encoding: utf-8 -*-
$:.push File.expand_path("../lib", __FILE__)
require 'selendroid/version'

Gem::Specification.new do |gem|
  gem.name          = "selendroid"
  gem.platform      = Gem::Platform::RUBY
  gem.version       = Selendroid::VERSION
  gem.authors       = ["Dominik Dary"]
  gem.email         = ["ddary@acm.org"]
  gem.homepage      = "http://selendroid.io"
  gem.description   = %q{"Selenium for Android Apps" (Test automate native or hybrid Android apps with Selendroid.)}
  gem.summary       = %q{Shell utility for selendroid.}
  ignores = File.readlines('.gitignore').grep(/\S+/).map {|s| s.chomp }
  dotfiles = [ '.gitignore']
  gem.files = (Dir["**/*"].reject { |f| File.directory?(f) || ignores.any? { |i| File.fnmatch(i, f) } } + dotfiles).sort

  gem.required_ruby_version = '>= 1.9.2'
  gem.executables   = "selendroid"
  gem.require_paths = ["lib"]
  gem.add_dependency( "colorize")
  gem.add_dependency( "retriable")
  gem.add_dependency( "awesome_print")
  gem.add_dependency( "selenium-webdriver")
  gem.add_dependency( "json")
end
