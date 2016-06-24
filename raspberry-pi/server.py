from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import SocketServer
import json as simplejson
import random
from urlparse import parse_qs

class Server (BaseHTTPRequestHandler):

    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def not_authorized(self):
        self.send_response(401)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def not_found(self):
        self.send_response(404)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):

        print 'Anfrage : '+ self.path

        # check authorization
        if (self.headers.getheader('auth-token') is None) or (self.headers.getheader('auth-token') != 'gurke'):
            self.not_authorized()
            return

        print parse_qs(self.path[2:])
        if parse_qs(self.path[2:]) == {}:
            print 'no action found'
            self.not_found()
            return

        action = parse_qs(self.path[2:]).get('action')[0]


        print 'recieved action = '+ action
        self._set_headers()
        self.wfile.write("action = " + action)

    def do_HEAD(self):
        print 'header method'
        self._set_headers()

    def do_POST(self):
        print 'post method...do nothing'

        return


def run(server_class=HTTPServer, handler_class=Server, port=80):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print 'Starting httpd...'
    httpd.serve_forever()

if __name__ == "__main__":
    from sys import argv

if len(argv) == 2:
    run(port=int(argv[1]))
else:
    run()