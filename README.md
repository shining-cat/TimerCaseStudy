# Timer Case Study
This is a sandbox playground to experiment various ways to build a timer.
## Usecase
Finding a way to precisely measure time so a Session, built out of Steps, will proceed correctly along time.
## Issue
The resulting session is longer than expected.
## Process
I built this project to be able to easily tweak the Timer implementation to study the origin of the drift and fix it.
## Concretely
* a Compose screen that display buttons to launch various implementations. It will display the progression of the session and steps (time remaining in seconds + a progressbar)
* a ViewModel that reacts to the button presses, instanciates the timer usecases and manipulates them to proceed through the Session
* the test Session is a hardcoded one provided by TestTimerSessionProvider. It lasts a total of 155s (theoretically)
* StepTimerUseCase is an interface to facilitate its implementation and manipulation by the viewmodel
* lastly, various attempts at implementing StepTimerUseCase, linked to the buttons in the screen via the VM, to easily compare the results
* The logcat will output some information along the duration of the Session, and a whole summary of the steps states when it ends
