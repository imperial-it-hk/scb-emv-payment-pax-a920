# Setting Description:
ConfigFirstActivity:Level 1 menu ,retrieved from arrays in the resource file (adapter corresponding to ConfigFirstAdapter)
ConfigSecondActivity:Level 2 menu like Comm,EDC setting ,etc.
ConfigThirdActivity:Level 3 menu , rely on ConfigInflater(like ConfigSelectInflater)
ConfigInflater:It mainly replaces the ViewStub layout. The main functions are also basically placed in various types of Inflaters.

Generally, after entering the settings interface, the pages are jumped from one level to three levels step by step.
Of course, there is also a jump from the first level to the third level: for example, the first few settings of the first level menu,like Password, Other settings.
There are also only two levels, no three levels of settings: such as switch settings, etc.

# buildType
1. autoTest
2. debug
3. release

release type is used for production environment(debuggable = false,also can't use screencap)
autoTest type is used for pre-production, The only difference between it and the production environment is the use of a debug key.(debuggable = false,but can use screencap)
debug type is used for developer, it contains many tools for convenient debug.