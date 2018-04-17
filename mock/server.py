#!/usr/bin/env python

from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import SocketServer


class S(BaseHTTPRequestHandler):
    def _set_headers(self, content_type='text/html'):
        self.send_response(200)
        self.send_header('Content-Type', content_type)
        self.end_headers()

    def do_HEAD(self):
        self._set_headers()

    def do_GET(self):
        self._set_headers()
        self.wfile.write('<html><body><h1>hi!</h1></body></html>')

    def do_POST(self):
        self._set_headers(content_type='application/json')
        self.wfile.write("""
            {
                "errCode": 0,
                "errName": null,
                "errMsg": "success",
                "wrapped": true,
                "data": {
                    "gitpath": "",
                    "imagepath": "",
                    "teamId": ,
                    "teamCode": "",
                    "imagerepouser": "",
                    "userid": ,
                    "imagerepopassword": "",
                    "taskid":
                }
            }
        """)


def run(server_class=HTTPServer, handler_class=S, port=80):
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
