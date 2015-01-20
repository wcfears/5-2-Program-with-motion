/**
•5-2 Day Water Heater
*
•Copyright 2015 Jim Allen
*
•Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
•in compliance with the License. You may obtain a copy of the License at:
*
• http://www.apache.org/licenses/LICENSE-2.0
*
•Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
•on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
•for the specific language governing permissions and limitations under the License.
*
*/
definition(
name: "5-2 Day Water Heater",
namespace: "Home Systems",
author: "Jim Allen",
description: "This App allows to schedule 2 weekday and 2 weekend schedules",
category: 	"Green Living",
iconUrl: 	"https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving.png",
iconX2Url:	"https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png"
)

preferences {
	section("Choose Switch ") {
	input name: "switches", type: "capability.switch", multiple: true
	}
section("Monday to Friday Schedule") {
	input "time1", "time", title: "Trun On Time Weekdays AM", required: true
	input "time2", "time", title: "Turn Off Time Weekdays AM", required: true
	input "time3", "time", title: "Turn On Time Weekdays PM", required: true
	input "time4", "time", title: "Turn Off Time Weekdays PM", required: true
}
section("Saturday and Sunday Schedule") {
	input "time11", "time", title: "Turn On Time Weekend AM", required: true
	input "time21", "time", title: "Turn Off Time Weekend AM", required: true
	input "time31", "time", title: "Turn On Time Weekend PM", required: true
	input "time41", "time", title: "Turn Off Time Weekend PM", required: true
}

}

def installed()
{
	subscribeToEvents()
 log.debug "Installed with settings: ${settings}"
}

def updated()
{
 unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(location, modeChangeHandler)
initialize()

}

// Handle mode changes, reinitialize the current temperature and timers after a mode change, this is to workaround the issue of the last timer firing while in a non running mode, resume operations when supported modes are set
def modeChangeHandler(evt) {
	log.debug "Reinitializing thermostat on mode change notification, new mode $evt.value"
	//sendNotificationEvent("$thermostat Reinitializing on mode change notification, new mode $evt.value")
 initialize()
}

// This section determines which day it is.
def initialize() {
unschedule()
def calendar = Calendar.getInstance()
calendar.setTimeZone(location.timeZone)
def today = calendar.get(Calendar.DAY_OF_WEEK)
def timeNow = now()
def midnightToday = timeToday("2000-01-01T23:59:59.999-0000", location.timeZone)
log.debug("Current time is ${(new Date(timeNow)).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
log.debug("Midnight today is ${midnightToday.format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
log.trace("Weekday schedule1 ${timeToday(time1, location.timeZone).format("HH:mm z", location.timeZone)}")
log.trace("Weekday schedule2 ${timeToday(time2, location.timeZone).format("HH:mm z", location.timeZone)}")
log.trace("Weekday schedule3 ${timeToday(time3, location.timeZone).format("HH:mm z", location.timeZone)}")
log.trace("Weekday schedule4 ${timeToday(time4, location.timeZone).format("HH:mm z", location.timeZone)}")
log.trace("Weekend schedule1 ${timeToday(time11, location.timeZone).format("HH:mm z", location.timeZone)}")
log.trace("Weekend schedule2 ${timeToday(time21, location.timeZone).format("HH:mm z", location.timeZone)}")
log.trace("Weekend schedule3 ${timeToday(time31, location.timeZone).format("HH:mm z", location.timeZone)}")
log.trace("Weekend schedule4 ${timeToday(time41, location.timeZone).format("HH:mm z", location.timeZone)}")

// This section is where the time/temperature schedule is set
switch (today) {
	case Calendar.MONDAY:
	case Calendar.TUESDAY:
	case Calendar.WEDNESDAY:
	case Calendar.THURSDAY:
    	if (timeNow >= timeToday(time1, location.timeZone).time && timeNow < timeToday(time2, location.timeZone).time) { // Are we between 1st time and 2nd time
        	schedule(timeToday(time2, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time2, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time2, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
    		log.debug "Turning on water heater"
            switches.on()
        }
    	else if (timeNow >= timeToday(time2, location.timeZone).time && timeNow < timeToday(time3, location.timeZone).time) { // Are we between 2nd time and 3rd time
			schedule(timeToday(time3, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time3, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time3, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	log.debug "Turning off water heater"
            switches.off()
        }
        else if (timeNow >= timeToday(time3, location.timeZone).time && timeNow < timeToday(time4, location.timeZone).time) { // Are we between 3rd time and 4th time
			schedule(timeToday(time4, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time4, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time4, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	log.debug "Turning on water heater"
            switches.on()
        }
        else if (timeNow >= timeToday(time4, location.timeZone).time && timeNow < midnightToday.time) { // Are we between 4th time and midnight, schedule next day
			schedule(timeToday(time1, location.timeZone) + 1, initialize)
            log.info("$thermostat Scheduled next adjustment for ${(timeToday(time1, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${(timeToday(time1, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	log.debug "Turning off water heater"
            switches.off()
        }
        else if (timeNow >= (midnightToday - 1).time && timeNow < timeToday(time1, location.timeZone).time) { // Are we between midnight yesterday and 1st time, schedule today
			schedule(timeToday(time1, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time1, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time1, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
			log.debug "Turning on water heater"
			switches.on()
        }
		break

	case Calendar.FRIDAY:
    	if (timeNow >= timeToday(time1, location.timeZone).time && timeNow < timeToday(time2, location.timeZone).time) { // Are we between 1st time and 2nd time
        	schedule(timeToday(time2, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time2, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time2, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
			log.debug "Turning on water heater"
            switches.on()
		}
    	else if (timeNow >= timeToday(time2, location.timeZone).time && timeNow < timeToday(time3, location.timeZone).time) { // Are we between 2nd time and 3rd time
			schedule(timeToday(time3, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time3, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time3, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	log.debug "Turning off water heater"
            switches.off()
        }
        else if (timeNow >= timeToday(time3, location.timeZone).time && timeNow < timeToday(time4, location.timeZone).time) { // Are we between 3rd time and 4th time
			schedule(timeToday(time4, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time4, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time4, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	log.debug "Turning on water heater"
            switches.on()
        }
        else if (timeNow >= timeToday(time4, location.timeZone).time && timeNow < midnightToday.time) { // Are we between 4th time Friday and midnight, we schedule Saturday
			schedule(timeToday(time11, location.timeZone) + 1, initialize)
            log.info("$thermostat Scheduled next adjustment for ${(timeToday(time11, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${(timeToday(time11, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	log.debug "Turning off water heater"
            switches.off()
        }
        else if (timeNow >= (midnightToday - 1).time && timeNow < timeToday(time11, location.timeZone).time) { // Are we between midnight Friday and 1st time on Saturday, we schedule Saturday
			schedule(timeToday(time11, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time11, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time11, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
			log.debug "Turning on water heater"
            switches.on()
        }
		break

	case Calendar.SATURDAY:
        if (timeNow >= timeToday(time11, location.timeZone).time && timeNow < timeToday(time21, location.timeZone).time) { // Are we between 1st time and 2nd time
        	schedule(timeToday(time21, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time21, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time21, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
   			log.debug "Turning on water heater"
            switches.on()
        }
    	else if (timeNow >= timeToday(time21, location.timeZone).time && timeNow < timeToday(time31, location.timeZone).time) { // Are we between 2nd time and 3rd time
   			schedule(timeToday(time31, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time31, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time31, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
			log.debug "Turning off water heater"
            switches.off()
        }
        else if (timeNow >= timeToday(time31, location.timeZone).time && timeNow < timeToday(time41, location.timeZone).time) { // Are we between 3rd time and 4th time
			schedule(timeToday(time41, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time41, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time41, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
			log.debug "Turning on water heater"
            switches.on()
        }
        else if (timeNow >= timeToday(time41, location.timeZone).time && timeNow < midnightToday.time) { // Are we between 4th time and midnight, schedule the next day
			schedule(timeToday(time11, location.timeZone) + 1, initialize)
            log.info("$thermostat Scheduled next adjustment for ${(timeToday(time11, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${(timeToday(time11, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	log.debug "Turning off water heater"
            switches.off()
        }
        else if (timeNow >= (midnightToday - 1).time && timeNow < timeToday(time11, location.timeZone).time) { // Are we between midnight yesterday and 1st time, schedule today
			schedule(timeToday(time11, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time11, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
			//sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time11, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	log.debug "Turning on water heater"
            switches.on()
        }
		break

	case Calendar.SUNDAY:
        if (timeNow >= timeToday(time11, location.timeZone).time && timeNow < timeToday(time21, location.timeZone).time) { // Are we between 1st time and 2nd time
    		schedule(timeToday(time21, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time21, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time21, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
			log.debug "Turning on water heater"        		
            switches.on()
        }
    	else if (timeNow >= timeToday(time21, location.timeZone).time && timeNow < timeToday(time31, location.timeZone).time) { // Are we between 2nd time and 3rd time
        	schedule(timeToday(time31, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time31, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time31, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            log.debug "Turning off water heater"
            switches.off()
        }
        else if (timeNow >= timeToday(time31, location.timeZone).time && timeNow < timeToday(time41, location.timeZone).time) { // Are we between 3rd time and 4th time
        	schedule(timeToday(time41, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time41, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time41, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	log.debug "Turning on water heater"
            switches.on()
        }
        else if (timeNow >= timeToday(time41, location.timeZone).time && timeNow < midnightToday.time) { // Are we between 4th time Sunday and midnight, we schedule Monday
        	schedule(timeToday(time1, location.timeZone) + 1, initialize)
            log.info("$thermostat Scheduled next adjustment for ${(timeToday(time1, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${(timeToday(time1, location.timeZone) + 1).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
        	log.debug "Turning off water heater"
            switches.off()
        }
        else if (timeNow >= (midnightToday - 1).time && timeNow < timeToday(time1, location.timeZone).time) { // Are we between midnight Sunday and 1st time on Monday, we schedule Monday
			schedule(timeToday(time1, location.timeZone), initialize)
            log.info("$thermostat Scheduled next adjustment for ${timeToday(time1, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
            //sendNotificationEvent("$thermostat Scheduled next adjustment for ${timeToday(time1, location.timeZone).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
			log.debug "Turning on water heater"
            switches.on()
        }
		break
 }
}
