/**
 * Created by Nicholas Azar on 2017-07-05.
 */

import React, {Component} from 'react';
import {Form, Upload, Icon} from 'antd';

const FormItem = Form.Item;

class SelectConfigForm extends Component {

    normFile = (e) => {
        console.log('Upload event:', e);
        if (Array.isArray(e)) {
            return e;
        }
        return e && e.fileList;
    };

    render() {
        const {getFieldDecorator} = this.props.form;

        return (
            <Form layout="vertical">
                <FormItem label="Config:">
                    <div className="dropbox">
                        {getFieldDecorator('dragger', {
                            valuePropName: 'fileList',
                            getValueFromEvent: this.normFile,
                        })(
                            <Upload.Dragger name="files" action="/upload.do">
                                <p className="ant-upload-drag-icon">
                                    <Icon type="inbox"/>
                                </p>
                                <p className="ant-upload-text">Click or drag the file to this area to upload</p>
                                <p className="ant-upload-hint">The config must be in JSON format.</p>
                            </Upload.Dragger>
                        )}
                    </div>
                </FormItem>
            </Form>
        )
    }
}

export default Form.create()(SelectConfigForm);