//0x00-0x63 General function
//0x00-0x2f decoder control
#define     REGISTER                0         // register function
#define     MOVE_UP                 1         // PTZ up
#define     MOVE_UP_STOP            2         //The gimbal stops up
#define     MOVE_DOWN               3         // gimbal down
#define     MOVE_DOWN_STOP          4         //The gimbal stops down
#define     MOVE_LEFT               5         // gimbal to the left
#define     MOVE_LEFT_STOP          6         //The gimbal stops to the left
#define     MOVE_RIGHT              7         // gimbal to the right
#define     MOVE_RIGHT_STOP         8         //The gimbal stops to the right
#define     MOVE_UP_LEFT            9         // top left of the gimbal
#define     MOVE_UP_LEFT_STOP       10          //The gimbal stops at the top left
#define     MOVE_UP_RIGHT           11        // top right of the gimbal
#define     MOVE_UP_RIGHT_STOP      12        //The gimbal stops at the top right
#define     MOVE_DOWN_LEFT          13        // bottom left of the gimbal
#define     MOVE_DOWN_LEFT_STOP     14        //The gimbal stops at the bottom left
#define     MOVE_DOWN_RIGHT         15        // bottom right of the gimbal
#define     MOVE_DOWN_RIGHT_STOP    16        //The gimbal stops at the bottom right
#define     SET_LEFT_BORDER         17        //set the left border
#define     SET_RIGHT_BORDER        18        //set the right border
#define     SET_UP_BORDER           19        //set the upper bound
#define     SET_DOWN_BORDER         20        //set the lower bound
#define     HOR_AUTO                21        //horizontal auto
#define     HOR_AUTO_STOP           22        //Automatic stop horizontally
#define     SET_HOR_AUTO_BEGIN      23        //Set the horizontal automatic start point
#define     SET_HOR_AUTO_END        24        //Set the horizontal automatic end point
#define     SET_HOR_AUTO_SPEED      25        //Set the horizontal auto speed
#define     VER_AUTO                26        // vertical auto
#define     SET_VER_AUTO_BEGIN      27        //Set the vertical automatic start point
#define     SET_VER_AUTO_END        28        //Set the vertical automatic end point
#define     SET_VER_AUTO_SPEED      29        //Set vertical auto speed
#define     VER_AUTO_STOP           30        //Auto stop vertically

#define     ZOOM_BIG                31        // zoom in
#define     ZOOM_BIG_STOP           32        // zoom and stop
#define     ZOOM_SMALL              33        // zoom small
#define     ZOOM_SMALL_STOP         34        // zoom small stop
#define     FOCUS_FAR               35        // focus far
#define     FOCUS_FAR_STOP          36        //Focus far stop
#define     FOCUS_NEAR              37        // focus close
#define     FOCUS_NEAR_STOP         38        //Focus near stop
#define     IRIS_OPEN               39        // aperture open
#define     IRIS_OPEN_STOP          40        //Aperture open and stop
#define     IRIS_CLOSE              41        //Aperture close
#define     IRIS_CLOSE_STOP         42        //stop the aperture

#define     LIGHT_ON                43        // light on
#define     LIGHT_OFF               44        // light off
#define     POWER_ON                45        // power on
#define     POWER_OFF               46        // power off
#define     RAIN_ON                 47        // wiper on
#define     RAIN_OFF                48        // wiper off
#define     TALK_ON                 49        // talk on
#define     TALK_OFF                50        // Intercom off
#define     DEF_ON                  51        //arm
#define     DEF_OFF                 52        //disarm

#define     BROWSE                  53        //Inquire
#define     ALARM_ANSWER            54        // alarm response
#define     STATE_ASK               55        //Request upload control status


//0x30-0x4f fastball control
#define     MOVE                    60        //fastball action
#define     MOVE_STOP               61        //stop the fast ball
#define     CALL_VIEW               62        // Attraction call
#define     SET_VIEW                63        // Attraction preset
#define     DELETE_VIEW             64        //delete preset attractions
#define     SEQUENCE_BEGIN          65        // start cruising
#define     SEQUENCE_END            66        //stop cruising
#define     ADD_PRESET              67        //add cruise point
#define     DELETE_PRESET           68        // delete cruise point
#define     CLEAR_SEQUENCE          69        //Clear cruise settings
#define     TIME_SEQUENCE           70        //set cruise time
#define     SET_GUARD               71        //set guard bit
#define     DELETE_GUARD            72        //delete guard
#define     GET_HOR_AUTO_SPEED      73        //get horizontal auto speed
#define     GET_TRACK_TIME          74        //get cruise time
#define     GET_TRACK_SEQUENCE      75        // get cruise sequence
#define     GUARD_TIME              76        //Set the time of the guard position
#define     GET_VIEW_LIST           77        //Get the sequence of sights

#define     MEMU_OPEN				78        //Open the menu Call 95
#define     MEMU_CLOSE              79        //Close the menu Call 96

//0x64-0xc7 dedicated function
#define     SWITCH                  101        //Manual matrix switch
#define     SWITCH_MONITOR          102        // cut monitor
#define     SWITCH_VIDICON          103        //Cut-off point
#define     SWITCH_ORDER            104        //sequence switch
#define     SWITCH_ORDER_STOP       105        //Sequential switch stop
#define     SWITCH_GROUP            106        //group switch
#define     SWITCH_GROUP_STOP       107        // group switch stop

#define     PROTOCOL_MOVE_UP                 1			//ptz up
#define     PROTOCOL_MOVE_DOWN               2			//ptz down
#define     PROTOCOL_MOVE_LEFT               3			//ptz left
#define     PROTOCOL_MOVE_RIGHT              4			//ptz right
#define     PROTOCOL_MOVE_UP_RIGHT           5			//ptz top right
#define     PROTOCOL_MOVE_UP_LEFT            6			//ptz top left
#define     PROTOCOL_MOVE_DOWN_RIGHT         7			//ptz down right
#define     PROTOCOL_MOVE_DOWN_LEFT          8			//ptz down left
#define     PROTOCOL_MOVE_STOP				 9			//stop ptz up
#define     PROTOCOL_ZOOM_BIG                10        // zoom in
#define     PROTOCOL_ZOOM_SMALL              11        // zoom small

#define     PROTOCOL_FOCUS_NEAR              13        // focus close
#define     PROTOCOL_FOCUS_FAR               14        // focus far

#define     PROTOCOL_IRIS_OPEN               17        // aperture open
#define     PROTOCOL_IRIS_CLOSE              18        //Aperture close
#define     PROTOCOL_RAIN_ON                 19        // wiper on
#define     PROTOCOL_RAIN_OFF                20        // wiper off
#define     PROTOCOL_LIGHT_ON                21        // light on
#define     PROTOCOL_LIGHT_OFF               22        // light off
#define     PROTOCOL_HOR_AUTO				 23		  //horizontal auto
#define     PROTOCOL_HOR_AUTO_STOP			 24		  //stop horizontal auto
#define     PROTOCOL_CALL_VIEW				 25		  //call preset

#define     PROTOCOL_SET_VIEW				 28		  //set preset
#define     PROTOCOL_POWER_ON                29        // power on
#define     PROTOCOL_POWER_OFF               30        // power off
