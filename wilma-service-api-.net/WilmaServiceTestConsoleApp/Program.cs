﻿using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using epam.wilma_service_api;

namespace WilmaServiceTestConsoleApp
{
    internal class Program
    {
        private static void Main(string[] args)
        {
            var wsConf = new WilmaServiceConfig("http://ESYJPB-SZG", 1234);
            var ws = new WilmaService(wsConf, new Logger());

           // ws.GetVersionInformationAsync().ContinueWith(res => { Console.WriteLine(res.Result); });
           // ws.GetActualLoadInformationAsync().ContinueWith(res => { Console.WriteLine(res.Result); });
           // ws.GetMessageLoggingStatusAsync().ContinueWith(res => { Console.WriteLine(res.Result); });

           // ws.SetMessageLoggingStatusAsync(WilmaService.MessageLoggingControlStatus.On).ContinueWith(res => { if (res.Result) { ws.GetMessageLoggingStatusAsync().ContinueWith(res1 => { Console.WriteLine(res1.Result); }); } });

           // ws.SetOperationModeAsync(WilmaService.OperationMode.WILMA).ContinueWith(res1 => { ws.GetOperationModeAsync().ContinueWith(res => { Console.WriteLine(res.Result); }); });

           // ws.SetLocalhostBlockingStatusAsync(WilmaService.LocalhostControlStatus.On).ContinueWith(res1 => { ws.GetLocalhostBlockingStatusAsync().ContinueWith(res => { Console.WriteLine(res.Result); }); });

           //// ws.ShutdownApplicationAsync().ContinueWith(res => { Console.WriteLine("Wilma shuted down: {0}", res.Result);});
           // ws.GetStubConfigInformationAsync().ContinueWith(res => { Console.WriteLine(res.Result); });

            //ws.ChangeStubConfigStatusAsync("EPAMNEWS", WilmaService.StubConfigStatus.ENABLED).ContinueWith(res => { Console.WriteLine(res.Result); });
            //ws.ChangeStubConfigOrderAsync("EPAMNEWS", WilmaService.StubConfigOrder.Up).ContinueWith(res => { Console.WriteLine(res.Result); });
            //ws.DropStubConfigAsync("EPAMNEWS").ContinueWith(res => { Console.WriteLine(res.Result); });

            //ws.PersistActualStubConfigAsync().ContinueWith(res => { Console.WriteLine(res.Result); });

            UpLoadFile(@"d:\wilma\proba.json", ws.UploadConditionCheckerAsync).ContinueWith(res => { Console.WriteLine(res.Result); });
            UpLoadFile(@"d:\wilma\proba.json", ws.UploadTemplateAsync).ContinueWith(res => { Console.WriteLine(res.Result); });
            UpLoadFile(@"d:\wilma\proba.json", ws.UploadTemplateFormatterAsync).ContinueWith(res => { Console.WriteLine(res.Result); });
            UpLoadFile(@"d:\wilma\proba.json", ws.UploadStubConfigurationAsync).ContinueWith(res => { Console.WriteLine(res.Result); });

            Console.ReadLine();
        }

        private static async Task<bool> UpLoadFile(string filePath, Func<string, Stream, Task<bool>> func)
        {
            using (var fs = File.OpenRead(filePath))
            {
                var res = await func(Path.GetFileName(filePath), fs);
                return res;
            }
        }
    }

    public class Logger : WilmaService.ILogger
    {
        public void Debug(string format, params object[] prs)
        {
            Console.WriteLine(format, prs);
        }

        public void Warning(string format, params object[] prs)
        {
            Console.WriteLine(format, prs);
        }

        public void Error(string format, params object[] prs)
        {
            Console.WriteLine(format, prs);
        }

        public void Info(string format, params object[] prs)
        {
            Console.WriteLine(format, prs);
        }
    }
}
