require 'rubygems'
require 'irb/completion'
require 'irb/ext/save-history'
require 'colorize'
require 'awesome_print'

AwesomePrint.irb!

ARGV.concat [ "--readline",
              "--prompt-mode",
              "simple" ]

# 50 entries in the list
IRB.conf[:SAVE_HISTORY] = 50

# Store results in home directory with specified file name
IRB.conf[:HISTORY_FILE] = ".irb-history"

require 'selendroid/selendroid_driver'
start_selendroid_server(nil)
wait_for_selendroid_server
start_selendroid_client
puts "#################### Selendroid shell ###################".green
puts "Selendroid inspector: http://localhost:8090/inspector".green
puts "Client-driver is available under: driver".green
puts "For help type: helpme".green