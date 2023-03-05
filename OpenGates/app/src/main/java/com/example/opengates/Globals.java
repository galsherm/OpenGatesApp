package com.example.opengates;

public class Globals {

    public enum MainClassesInProj {
        MainActivity {
            public String toString() {
                return "MainActivity";
            }
        },
        LocationSettings {
            public String toString() {
                return "LocationSettings";
            }
        },
        Hours {
            public String toString() {
                return "Hours";
            }
        },
        Days {
            public String toString() {
                return "Days";
            }
        },
        PhoneCall {
            public String toString() {
                return "PhoneCall";
            }
        } , Service {
            public String toString() {
                return "Service";
            }
        } ,StoredData {
            public String toString() {
                return "StoredData";
            }
        }
    }

    public enum DaysInWeek {
        sunday {
            public String toString() {
                return "sunday";
            }
        },
        monday {
            public String toString() {
                return "monday";
            }
        },
        tuesday {
            public String toString() {
                return "tuesday";
            }
        },
        wednesday {
            public String toString() {
                return "wednesday";
            }
        },
        thursday {
            public String toString() {
                return "thursday";
            }
        },
        friday {
            public String toString() {
                return "friday";
            }
        },
        saturday {
            public  String toString() {
                return "saturday";
            }
        },

    }
    public static String sharedPrefName = "MySharedPref";
    public static String gitcMessage = "goingInToTheCircle";
    public static String difgMessage = "Distance from the gate";
    public static String serviceMessages = "my-message";
   // public static int waitInSeconds = 1;
}
