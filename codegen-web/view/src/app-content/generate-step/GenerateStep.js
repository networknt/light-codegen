/**
 * Created by Nicholas Azar on 2017-07-05.
 */

import React, {Component} from 'react';
import {Row, Col, Menu, Dropdown} from 'antd';
import Highlight from 'react-highlight'

import './GenerateStep.less';

class GenerateStep extends Component {

    onMenuItemClick = (e) => {
        console.log('Menu item click: ', e);
        if (e.key === 'add-another') {
            this.props.onAddAnother();
        }
    };

    onGenerateButtonClick = (e) => {
        console.log('Generate button click: ', e);
    };

    generate_button_menu = (
        <Menu onClick={this.onMenuItemClick}>
            <Menu.Item key="add-another">Add Another</Menu.Item>
        </Menu>
    );

    render() {
        return (
            <div style={{padding: '20px'}}>
                <Row type="flex" justify="center">
                    <Col xs={24} sm={18} md={14} lg={12} xl={6}>
                        <h2 style={{color: 'rgba(0, 0, 0, 0.5)'}}>Generate</h2>
                        <p>
                            Review the selected uploaded files and click generate
                        </p>
                    </Col>
                </Row>
                <div style={{height: '20px'}}/>
                <Row type="flex" justify="center">
                    <Col span={10}>
                        <Highlight className="prettyprint lang-json">{
                            "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Beatae distinctio ducimus eaque \n" +
                            "eos eum facere fugiat incidunt labore natus omnis quae quibusdam quo, quod recusandae \n" +
                            "repellendus totam, vel veniam vitae.\n" +
                            "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusantium adipisci animi \n" +
                            "corporis cupiditate delectus et, eum ipsum itaque iusto laboriosam neque obcaecati optio \n" +
                            "possimus quae saepe, sed sint temporibus tenetur."
                        }
                        </Highlight>
                    </Col>
                    <Col span={2}>
                    </Col>
                    <Col span={10}>
                        <Highlight className="prettyprint lang-json">{
                            "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Beatae distinctio ducimus eaque \n" +
                            "eos eum facere fugiat incidunt labore natus omnis quae quibusdam quo, quod recusandae \n" +
                            "repellendus totam, vel veniam vitae.\n" +
                            "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusantium adipisci animi \n" +
                            "corporis cupiditate delectus et, eum ipsum itaque iusto laboriosam neque obcaecati optio \n" +
                            "possimus quae saepe, sed sint temporibus tenetur."
                        }
                        </Highlight>
                    </Col>
                </Row>
                <Row type="flex" justify="left">
                    <Col span={2} offset={21}>
                        <Dropdown.Button onClick={this.onGenerateButtonClick} overlay={this.generate_button_menu} className="generate-button" type="primary" size="large">
                            Generate!
                        </Dropdown.Button>
                    </Col>
                </Row>
            </div>
        )
    }
}

export default GenerateStep;