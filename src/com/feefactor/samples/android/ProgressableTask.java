/*-
 * Copyright (c) 2010, NETMOBO LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *     i.   Redistributions of source code must retain the above copyright 
 *          notice, this list of conditions and the following disclaimer.
 *     ii.  Redistributions in binary form must reproduce the above copyright 
 *          notice, this list of conditions and the following disclaimer in the 
 *          documentation and/or other materials provided with the 
 *          distribution.
 *     iii. Neither the name of NETMOBO LLC nor the names of its contributors 
 *          may be used to endorse or promote products derived from this 
 *          software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.feefactor.samples.android;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

/**
 * @author netmobo
 */
public class ProgressableTask {

	private final Activity _context;
	private ProgressDialog _progressDialog;
	private final int _titleID;
	private final CharSequence _message;
	private final ProgressableRunnable _task;
	private Thread _taskThread;
	private int _cancelID = -1;

	private volatile boolean _isCancelled;

	public ProgressableTask(Activity context, ProgressableRunnable task,
			int titleID, int messageID, int cancelID) {
		this(context, task, titleID, messageID, cancelID, (Object[]) null);
	}

	public ProgressableTask(Activity context, ProgressableRunnable task,
			int messageID) {
		this(context, task, -1, messageID, -1, (Object[]) null);
	}

	public ProgressableTask(Activity context, ProgressableRunnable task,
			int titleID, int messageID, int cancelID, Object... args) {
		_context = context;
		_titleID = titleID;
		_message = args == null ? context.getString(messageID) : context
				.getString(messageID, args);
		_cancelID = cancelID;
		_task = task;
	}
	
	public void start() {
		showProgressBar();
		_taskThread = new Thread("ProgressableTask") {
			public void run() {
				try {
					_task.run();
				} finally {
					dismiss();
				}
			}
		};
		_taskThread.start();
	}

	public boolean isDone() {
		return _taskThread != null && (!_taskThread.isAlive() || isCancelled());
	}

	public boolean isCancelled() {
		return _isCancelled;
	}

	private void showProgressBar() {
		_progressDialog = new ProgressDialog(_context);
		if (_titleID != -1) {
			_progressDialog.setTitle(_titleID);
		}
		_progressDialog.setMessage(_message);
		if (_cancelID != -1) {
			_progressDialog.setButton(_context.getText(_cancelID),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							cancel();
						}
					});

			_progressDialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface arg0) {
					cancel();
				}
			});
		}
		_progressDialog.show();
	}

	public void cancel() {
		_isCancelled = true;
		dismiss();
		_taskThread.interrupt();
		_task.onCancel();
	}

	private void dismiss() {
		if (_progressDialog.isShowing()) {
			_progressDialog.dismiss();
		}
	}
}
