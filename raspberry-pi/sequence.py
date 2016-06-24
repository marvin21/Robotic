import RPi.GPIO as GPIO
import time
import os

class Sequence:

    GPIO.setwarnings(False)
    # Set the layout for the pin declaration
    GPIO.setmode(GPIO.BOARD)
    # The pin 11 is the out pin for the PWM signal for the servo.
    GPIO.setup(11, GPIO.OUT)
    GPIO.setup(13, GPIO.OUT)
    GPIO.setup(15, GPIO.OUT)

# initial Stellung vom Roboarm
    def initial_stellung (self):

        return

    def do_something(self):

        return
